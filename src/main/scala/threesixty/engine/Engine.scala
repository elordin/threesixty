package threesixty.engine

import threesixty.visualizer.Visualization

import spray.http.{HttpMethods, HttpResponse, HttpHeaders, HttpRequest, AllOrigins, StatusCodes, ContentTypes, ContentType, MediaTypes, HttpEntity, StatusCode}
import ContentTypes.{`text/plain(UTF-8)`, `application/json`}
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

object SuccessResponse {
    def apply(json: JsValue): SuccessResponse = SuccessResponse(json.prettyPrint, `application/json`)
}
case class SuccessResponse(msg: String, contentType: ContentType = `text/plain(UTF-8)`) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(contentType, msg),
            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
        )
}


case class VisualizationResponse(visualization: Visualization) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(`image/svg+xml`, visualization.toString),
            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
        )
}


object ErrorResponse {
    def apply(json: JsValue): ErrorResponse = ErrorResponse(json.toString, `application/json`)
}
case class ErrorResponse(msg: String, contentType: ContentType = `text/plain(UTF-8)`) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = StatusCodes.BadRequest,
            entity = HttpEntity(contentType, msg),
            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
        )
}

object HelpResponse {
    def apply(json: JsValue): HelpResponse = HelpResponse(msg = json.toString, contentType = `application/json`)
    def apply(json: JsValue, status: StatusCode): HelpResponse = HelpResponse(json.toString, status, `application/json`)
}
case class HelpResponse(msg: String, status: StatusCode = StatusCodes.OK, contentType: ContentType = `text/plain(UTF-8)`) extends EngineResponse {
    def toHttpResponse: HttpResponse = HttpResponse(
            status = status,
            entity = HttpEntity(contentType, msg),
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
