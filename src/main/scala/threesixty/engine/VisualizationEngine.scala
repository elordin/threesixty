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
                                        continue using both the original dataset unchanged and the
                                        processed one.

"""

    def processRequest(jsonString: String): EngineResponse = {
        val json = jsonString.parseJson.asJsObject

        val requestType: String = json.getFields("type")(0).convertTo[String]

        val result: EngineResponse =
            (requestType) match {
                case "visualization" => processVisualizationRequest(json)
                case "help"          => processHelpRequest(json)
                case _               => ErrorResponse(s"Unknown command: $requestType")
            }

        result
    }


    def processHelpRequest(json: JsObject): HelpResponse = {
        // TODO: Maybe add another param to switch between processor and visualizer help
        try {
            val helpFor = json.getFields("for")(0).convertTo[String]
            helpFor.toLowerCase match {
                case "visualizations" | "v" =>
                    val availablevisualizations = visualizer.visualizationInfos.keys
                    HelpResponse(availablevisualizations.foldLeft(
                        "{ \"visualizations\": [\n")(_ + "    \"" + _ + "\",\n") + "}")
                case "processingmethods" | "p" =>
                    ???
                case _ =>
                    val helper: UsageInfo = visualizer.visualizationInfos.getOrElse(
                        helpFor, ??? // get from processor
                    )
                    HelpResponse(helper.usage)
            }
        } catch {
            case _:Exception => HelpResponse(
                // get processor
                "Threesixty Engine\n" +
                "    use\n" +
                "       {\n" +
                "           \"type\" : \"help\",\n" +
                "           \"for\" : KEYWORD\n" +
                "       }\n" +
                "    where KEYWORD is either a visualization, " +
                "a processing method to get usage information " +
                "or \"list\" to show a list of available visualizations " +
                "and processing methods  \n")
        }
    }


    def processVisualizationRequest(json: JsObject): VisualizationResponse = {

        val vizConfigOption: Option[VisualizationConfig] = try {
            val vizConfigS: String = json.getFields("visualization")(0).toString
            Some(visualizer.toVisualizationConfig(vizConfigS))
        } catch {
            case _:Exception => // TODO limit
                None
        }

        val procStratOption:Option[ProcessingStrategy] = Some(ProcessingStrategy(
            ProcessingStep(LinearInterpolation(3,
                Map("data1" -> "data1i", "data2" -> "data2i")),
                Set[Identifier]("data1", "data2")
            ),
            ProcessingStep(LinearInterpolation(1,
                Map("data3" -> "data3i")),
                Set[Identifier]("data3")
            )
        ))
        /*
        val procStratOption: Option[ProcessingStrategy] = try {
            val processingSteps: Seq[ProcessingStep] = ???
                // json.getFields("processor")(0).convertTo[Seq[ProcessingStep]]

            Some(ProcessingStrategy(processingSteps: _* ))
        } catch {
            case _: Exception => // TODO limit
                None
        } */

        // TODO throws DeserializationException
        // TODO throws IndexOutOfBoundsException
        val dataIDs:Set[String] = json.getFields("data")(0).convertTo[Set[String]]


        val (processingStrategy, visualizationConfig): (ProcessingStrategy, VisualizationConfig) =
            (procStratOption, vizConfigOption) match {
                case (Some(procStrat:ProcessingStrategy), Some(vizConfig:VisualizationConfig)) => (procStrat, vizConfig)
                case (Some(procStrat), None)            => ???
                case (None, Some(vizConfig))            => ???
                case (None, None)                       => ???
            }

        val config: Config = new Config(dataIDs, dbAdapter)

        processingStrategy(config)
        VisualizationResponse(visualizationConfig(config))
    }
}
