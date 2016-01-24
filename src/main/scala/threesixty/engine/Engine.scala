package threesixty.engine

import threesixty.visualizer.Visualization

import spray.http.{HttpMethods, HttpResponse, HttpHeaders, HttpRequest, AllOrigins, StatusCodes, ContentTypes, MediaTypes, HttpEntity, StatusCode}
import ContentTypes.`text/plain(UTF-8)`
import MediaTypes.`image/svg+xml`
import HttpMethods.{GET, POST}
import HttpHeaders.`Access-Control-Allow-Origin`

import spray.json.{JsString, JsValue, JsObject}


trait UsageInfo {
    def usage: String
}


trait EngineResponse {
    def toHttpResponse: HttpResponse
}


case class VisualizationResponse(val visualization: Visualization) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(`image/svg+xml`, visualization.toString),
            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
        )
}


case class ErrorResponse(val msg: String) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = StatusCodes.BadRequest,
            entity = HttpEntity(`text/plain(UTF-8)`, msg),
            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
        )
}


case class HelpResponse(val msg: String, val status: StatusCode = StatusCodes.OK) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = status,
            entity = HttpEntity(`text/plain(UTF-8)`, msg),
            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
        )
}


object Engine {
    def toErrorJson(errorMsg: String): JsValue = {
        JsObject(Map[String, JsValue]("error" -> JsString(errorMsg)))
    }
}

trait Engine {
    def processRequest(jsonString: String): EngineResponse
}


trait HttpRequestProcessor extends Engine {
    def processRequest: (HttpRequest) => EngineResponse = {
        case HttpRequest(POST, _, _, body: HttpEntity.NonEmpty, _) =>
            this.processRequest(body.asString)
        case HttpRequest(POST, _, _, HttpEntity.Empty, _) =>
            ErrorResponse(Engine.toErrorJson("Empty request body.").toString)
        case _ => ErrorResponse(Engine.toErrorJson("Bad Request").toString)
    }
}
