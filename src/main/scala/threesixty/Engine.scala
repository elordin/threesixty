package threesixty.engine

import threesixty.processor.{Processor, ProcessingStrategy, ProcessingStep}
import threesixty.visualizer.{Visualizer, Visualization, VisualizationConfig}
import threesixty.persistence.DatabaseAdapter
import threesixty.config.Config

import spray.http.{HttpResponse, StatusCodes, ContentTypes, HttpEntity, StatusCode}
import ContentTypes.`text/plain(UTF-8)`

import spray.json._
import DefaultJsonProtocol._


trait EngineResponse {
    def toHttpResponse: HttpResponse
}


case class VisualizationResponse(val visualization: Visualization) extends EngineResponse {
    def toHttpResponse: HttpResponse = ???
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


case class Engine(processor: Processor, visualizer: Visualizer, dbAdapter: DatabaseAdapter) {

    def processRequest(jsonString: String): HttpResponse = {
        val json = jsonString.parseJson.asJsObject

        val requestType: String = json.getFields("type")(0).convertTo[String]

        val result: EngineResponse =
            (requestType) match {
                case "visualization" => processVisualizationRequest(json)
                case "help"          => processHelpRequest(json)
                case _               => ErrorResponse(s"Unknown command: $requestType")
            }

        result.toHttpResponse
    }


    def processHelpRequest(json: JsObject): HelpResponse = {
        HelpResponse("Help Text")
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
