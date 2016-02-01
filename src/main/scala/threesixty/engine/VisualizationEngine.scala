package threesixty.engine

import threesixty.processor.{Processor, ProcessingStrategy, ProcessingStep}
import threesixty.visualizer.{Visualizer, VisualizationConfig}
import threesixty.persistence.DatabaseAdapter

import threesixty.data.{DataPool, UnsafeInputData}
import threesixty.data.Data.Identifier
import threesixty.algorithms.interpolation.LinearInterpolation

import spray.http.HttpResponse
import spray.json._

import threesixty.data.DataJsonProtocol._


object VisualizationEngine {
    def using(p: Processor) = new AnyRef {
        def and(v: Visualizer) = new AnyRef {
            def and(d: DatabaseAdapter) = VisualizationEngine(p, v, d)
        }
    }

    def using(v: Visualizer) = new AnyRef {
        def and(p: Processor) = new AnyRef {
            def and(d: DatabaseAdapter) = VisualizationEngine(p, v, d)
        }
    }
}


/**
 *  Core Visualization engine. Processes a string (usually from a HTTP request)
 *  and return the requested resource, usually a visualization.
 *
 *  @author Thomas Weber
 *
 *  @param processor A processor object with available processing methods mixed in.
 *  @param visualizer A visualizer object with available visualizations mixed in.
 *  @param dbAdapter The adapter to the database holding all data.
 */
case class VisualizationEngine(
    processor: Processor,
    visualizer: Visualizer,
    dbAdapter: DatabaseAdapter
) extends Engine with HttpRequestProcessor with UsageInfo {

    def usage: String =
"""Threesixty - Visualization Engine

HELP
{                                       Returns usage information for modules of the engine.
    "type": "help",                     HELP_KEYWORD can be one of
    "for": HELP_KEYWORD                   - "visualizations"     Lists all available visualizations
}                                         - "processingmethods"  Lists all available processing methods
                                          - visualization_name   Shows arguments for this visualization
                                          - processing_method    Shows arguments for this processing method

VISUALIZATION
{                                       Returns a visualization as SVG
    "type": "visualization",
    "data": [ DATA_IDS ],               DATA_IDS is a list of IDs of the datasets for processing
    "visualization": {                  The "visualization" parameter is optional. If none is given it is deduced form the data.
        "type": VISUALIZATION_TYPE      VISUALIZATION_TYPE is the name of the visualization
        "args": VISUALIZATION_ARGS      VISUALIZATION_ARGS are the arguments of the visualization
    },
    "processor": [ PROCESSING_STEPS]    PROCESSING_STEPS is a list of processing steps
}                                       processing steps follow the format:
                                        {
                                            "method": "PROCESSING_METHOD",
                                            "args": PROCESSING_METHOD_ARGS
                                            "data": [ DATA_IDS ]
                                            "idmapping": [ ID_MAPPING ]
                                        }
                                        PROCESSING_METHOD is the name of the method.
                                        PROCESSING_METHOD_ARGS are the parameters for that method
                                        DATA_IDS is a list of IDs of datasets this method is applied to
                                        ID_MAPPING allows renaming the output dataset. Doing this allows
                                        to continue using both the original dataset unchanged and the
                                        processed one using their now different ids.
                                        If a dataset is processed whose ID is not in ID_MAPPING, it will
                                        be overridden with the processed result.
"""


    /**
     *  Processes an arbitrary request. Checks type parameter and delegates.
     *
     *  @author Thomas Weber
     *
     *  @param jsonString RequestBody, assumes JSON
     *  @return EngineResponse depending on type parameter
     */
    def processRequest(jsonString: String): EngineResponse = {
        try {
            val json = jsonString.parseJson.asJsObject

            val requestType: String = json.fields("type").convertTo[String]

            val result: EngineResponse =
                (requestType) match {
                    case "visualization" => processVisualizationRequest(json)
                    case "data"          => processDataRequest(json)
                    case "help"          => processHelpRequest(json)
                    case _               => ErrorResponse(Engine.toErrorJson(s"Unknown type: $requestType"))
                }

            result
        } catch {
            case e:JsonParser.ParsingException  => ErrorResponse(Engine.toErrorJson("Invalid JSON"))
            case e:NoSuchElementException       => ErrorResponse(Engine.toErrorJson("type parameter missing"))
        }
    }


    def processDataRequest(json: JsObject): EngineResponse = {
        try {
            val action =  json.fields("action").convertTo[String]
            action match {
                case "insert" => processDataInsertRequest(json)
                case "get"    => processDataGetRequest(json)
                case _        => ErrorResponse(Engine.toErrorJson("unknown action"))
            }
        } catch {
            case e: DeserializationException => ErrorResponse(Engine.toErrorJson("invalid format"))
            case e: NoSuchElementException   => ErrorResponse(Engine.toErrorJson("action parameter missing"))
        }
    }


    def processDataInsertRequest(json: JsObject): EngineResponse = {
        try {
            val data = json.fields("data").convertTo[UnsafeInputData]
            dbAdapter.appendOrInsertData(data) match {
                case Right(id) => SuccessResponse(JsObject(Map[String, JsValue](
                        "type" -> JsString("success"),
                        "id" -> JsString(id)
                    )))
                case Left(error)    => ErrorResponse(Engine.toErrorJson(error))
            }
        } catch {
            case e: DeserializationException =>
                ErrorResponse(Engine.toErrorJson("invalid format: " + e.getMessage))
            case e: NoSuchElementException   =>
                ErrorResponse(Engine.toErrorJson("data parameter missing"))
        }
    }

    def processDataGetRequest(json: JsObject): EngineResponse = {
        try {
            val id = json.fields("id").convertTo[Identifier]
            dbAdapter.getDataset(id) match {
                case Right(data) => SuccessResponse(JsObject(Map[String, JsValue](
                        "type" -> JsString("success"),
                        "data" -> data.toJson
                    )))
                case Left(error)    => ErrorResponse(Engine.toErrorJson(error))
            }
        } catch {
            case e: DeserializationException =>
                ErrorResponse(Engine.toErrorJson("invalid format"))
            case e: NoSuchElementException   =>
                ErrorResponse(Engine.toErrorJson("data parameter missing"))
        }
    }


    /**
     *  Processes a requests of type: help. Assumes "for" parameter
     *  to distinguish between help messages.
     *
     *  @author Thomas Weber
     *
     *  @return HelpResponse with requested help or generic help if "for" was missing.
     */
    def processHelpRequest(json: JsObject): EngineResponse =
        json.fields.get("for").map({
            case JsString(forJson) => forJson.toLowerCase match {
                case "data" => ???
                case "data-insert" => ???
                case "data-get" => ???

                case "visualizer" =>
                    HelpResponse(visualizer.usage)
                case "processor" =>
                    HelpResponse(processor.usage)
                case "visualizations" | "v" =>
                    val availablevisualizations = visualizer.visualizationInfos.keys
                    HelpResponse(JsObject(Map[String, JsValue]("visualizations" -> availablevisualizations.toJson)))
                case "processingmethods" | "p" =>
                    val availableMethods = processor.processingInfos.keys
                    HelpResponse(JsObject(Map[String, JsValue]("processingmethods" -> availableMethods.toJson)))
                case helpFor =>
                    visualizer
                        .visualizationInfos
                        .get(helpFor)
                        .map(_.usage)
                        .map(HelpResponse(_))
                        .getOrElse(
                            processor
                                .processingInfos
                                .get(helpFor)
                                .map(_.usage)
                                .map(HelpResponse(_))
                                .getOrElse(
                                    ErrorResponse(Engine.toErrorJson("Unknown help-for parameter."))
                                )
                        )
            }
            case _ => ErrorResponse(Engine.toErrorJson("Invalid format for for parameter."))
        }).getOrElse(HelpResponse(usage))


    /**
     *  Processes the request into a visualization.
     *  Reads the visualization parameter as VisualizationConfig or
     *  deduces one if none is given.
     *  Reads the processor parameter as ProcessingStrategy or
     *  deduces one if none is given.
     *  Assumes a list of data IDs at "data".
     *
     *  @author Thomas Weber
     *
     *  @param json JsObject parsed from the initial request.
     *  @return VisualizationResponse on success, ErrorResponse with error message on failure.
     */
    def processVisualizationRequest(json: JsObject): EngineResponse = {
        val vizConfigOption: Option[VisualizationConfig] = try {
            json.fields.get("visualization").map {
                viz: JsValue => visualizer.toVisualizationConfig(viz.toString)
            }
        } catch {
            case e:NoSuchElementException =>
                return ErrorResponse(Engine.toErrorJson(e.getMessage).toString) // Should be: Unknown visualization
            case e:IllegalArgumentException =>
                return ErrorResponse(Engine.toErrorJson(e.getMessage).toString) // Should be: Parameter missing
        }

        val procStratOption: Option[ProcessingStrategy] = try {
            json.fields.get("processor").map {
                proc: JsValue => processor.toProcessingStrategy(proc.toString)
            }
        } catch {
            case e:NoSuchElementException =>
                return ErrorResponse(Engine.toErrorJson(e.getMessage).toString) // Should be: Unknown method
            case e:IllegalArgumentException =>
                return ErrorResponse(Engine.toErrorJson(e.getMessage).toString) // Should be: Parameter missing
        }

        val dataIDs:Set[Identifier] = json.fields.getOrElse("data",
                return ErrorResponse(Engine.toErrorJson("data parameter missing.").toString)
            ).convertTo[Set[Identifier]]


        val dataPool: DataPool = new DataPool(dataIDs, dbAdapter)

        val (processingStrategy, visualizationConfig): (ProcessingStrategy, VisualizationConfig) =
            (procStratOption, vizConfigOption) match {
                case (Some(procStrat:ProcessingStrategy), Some(vizConfig:VisualizationConfig)) =>
                    (procStrat, vizConfig)
                case (Some(procStrat), None) =>
                    println("Viz missing"); (procStrat, ???) // TODO deduction
                case (None, Some(vizConfig)) =>
                    println("ProcStrat missing"); (???, vizConfig) // TODO deduction
                case (None, None) =>
                    println("Both missing"); (???, ???)       // TODO deduction
            }


        // Apply processing Methods
        processingStrategy(dataPool)

        // return Visualization
        VisualizationResponse(visualizationConfig(dataPool))
    }
}
