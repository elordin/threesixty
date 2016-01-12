package threesixty.server

import akka.actor.{Actor, Props}
import akka.event.Logging
import spray.http.{HttpMethods, MediaTypes, HttpEntity, HttpResponse, HttpRequest, StatusCodes}
import spray.can.Http

import HttpMethods.{GET, POST}
import MediaTypes.`application/json`


object APIHandler {
    def props: Props = Props(new APIHandler )
}

/**
 *  Reads the HTTP request and dispatches it to an EngineActor
 */
class APIHandler extends Actor {
    val log = Logging(context.system, this)

    def receive = {
        case HttpRequest(POST, _, _, _, _) =>
            // TODO parse body as json to Config
            // TODO initialize data processing
            // TODO await visualization response and send as response
            sender ! HttpResponse(
                entity = HttpEntity(`application/json`,
                "{\"test\": 1}"))

        case _: Http.ConnectionClosed =>
            context stop self

        case msg =>
            log.error("Unknown message: " + msg)
            sender ! HttpResponse(
                status = StatusCodes.MethodNotAllowed,
                entity = HttpEntity(
                    `application/json`,
                    """{ "error" : "unknown message "}"""))
    }

}
