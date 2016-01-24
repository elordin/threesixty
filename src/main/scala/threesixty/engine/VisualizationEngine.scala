package threesixty.engine

import threesixty.processor.{Processor, ProcessingStrategy, ProcessingStep}
import threesixty.visualizer.{Visualizer, VisualizationConfig}
import threesixty.persistence.DatabaseAdapter
import threesixty.config.Config

import threesixty.data.Data.Identifier
import threesixty.algorithms.interpolation.LinearInterpolation

import spray.http.HttpResponse
import spray.json._

import DefaultJsonProtocol._


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

            val requestType: String = json.getFields("type")(0).convertTo[String]

            val result: EngineResponse =
                (requestType) match {
                    case "visualization" => processVisualizationRequest(json)
                    // TODO case "data" => processInsertData
                    case "help"          => processHelpRequest(json)
                    case _               => ErrorResponse(Engine.toErrorJson(s"Unknown type: $requestType").toString)
                }

            result
        } catch {
            case e:JsonParser.ParsingException => println(jsonString); ErrorResponse(Engine.toErrorJson("Invalid JSON").toString)
            case e:IndexOutOfBoundsException => ErrorResponse(Engine.toErrorJson("type parameter missing").toString)
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
    def processHelpRequest(json: JsObject): EngineResponse = {
        try {
            val helpFor = json.getFields("for")(0).convertTo[String]
            helpFor.toLowerCase match {
                case "visualizer" =>
                    HelpResponse(visualizer.usage)
                case "processor" =>
                    HelpResponse(processor.usage)
                case "visualizations" | "v" =>
                    val availablevisualizations = visualizer.visualizationInfos.keys
                    HelpResponse(JsObject(Map[String, JsValue]("visualizations" -> availablevisualizations.toJson)).toString)
                case "processingmethods" | "p" =>
                    val availableMethods = processor.processingInfos.keys
                    HelpResponse(JsObject(Map[String, JsValue]("processingmethods" -> availableMethods.toJson)).toString)
                case _ =>
                    ErrorResponse(Engine.toErrorJson("Unknown help-for parameter.").toString)
            }
        } catch {
            // No "for" given
            case e:IndexOutOfBoundsException => HelpResponse(usage)
        }
    }


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

        val config: Config = new Config(dataIDs, dbAdapter)

        // Apply processing Methods
        processingStrategy(config)

        // return Visualization
        VisualizationResponse(visualizationConfig(config))
    }
}
