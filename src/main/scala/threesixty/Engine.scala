package threesixty.engine

import threesixty.processor.{Processor, ProcessingStrategy, ProcessingStep}
import threesixty.visualizer.{Visualizer, Visualization, VisualizationConfig}
import threesixty.persistence.DatabaseAdapter
import threesixty.config.Config

import spray.http.{HttpResponse, StatusCodes, ContentTypes, MediaTypes, HttpEntity, StatusCode}
import ContentTypes.`text/plain(UTF-8)`
import MediaTypes.`image/svg+xml`

import spray.json._
import DefaultJsonProtocol._


trait UsageInfo {
    def usage: String
}


trait EngineResponse {
    def toHttpResponse: HttpResponse
}


case class VisualizationResponse(val visualization: Visualization) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity(`image/svg+xml`, visualization.toString)
    )
}


case class ErrorResponse(val msg: String) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = StatusCodes.BadRequest,
            entity = HttpEntity(`text/plain(UTF-8)`, msg)
        )
}


case class HelpResponse(val msg: String, val status: StatusCode = StatusCodes.OK) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = status,
            entity = HttpEntity(`text/plain(UTF-8)`, msg)
        )
}


case class Engine(val processor: Processor, val visualizer: Visualizer, val dbAdapter: DatabaseAdapter) {

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
                case "list" =>
                    val availablevisualizations = visualizer.visualizationInfos.keys
                    HelpResponse(availablevisualizations.foldLeft(
                        "Available visualizations:\n")(_ + "    " + _ + "\n"))
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
            val vizConfigS: String = json.getFields("visualization")(0).convertTo[String]
            Some(visualizer.toVisualizationConfig(vizConfigS))
        } catch {
            case _:Exception => // TODO limit
                None
        }

        val procStratOption: Option[ProcessingStrategy] = try {
            val processingSteps: Seq[ProcessingStep] = ???
                // json.getFields("processor")(0).convertTo[Seq[ProcessingStep]]

            Some(ProcessingStrategy(processingSteps: _* ))
        } catch {
            case _: Exception => // TODO limit
                None
        }

        // TODO throws DeserializationException
        // TODO throws IndexOutOfBoundsException
        val dataIDs:Set[String] = json.getFields("data")(0).convertTo[Set[String]]


        val (processingStrategy, visualizationConfig): (ProcessingStrategy, VisualizationConfig) =
            (procStratOption, vizConfigOption) match {
                case (Some(procStrat), Some(vizConfig)) => (procStrat, vizConfig)
                case (Some(procStrat), None)            => ???
                case (None, Some(vizConfig))            => ???
                case (None, None)                       => ???
            }

        val config: Config = new Config(dataIDs, dbAdapter)

        processingStrategy(config)
        VisualizationResponse(visualizationConfig(config))
    }
}
