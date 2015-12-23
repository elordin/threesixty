package threesixty.server

import akka.actor.{Actor, Props}
import akka.event.Logging
import spray.http._
import spray.can.Http

import HttpMethods._
import MediaTypes._


object APIHandler {
    def props:Props = Props(new APIHandler )
}

/**
 *  Handles all interaction with the API, including
 *   - Reading and parsing of the request body
 *   - Conversion to Config object
 *   - Starting the processing job
 */
class APIHandler extends Actor {
    val log = Logging(context.system, this)

    def receive = {
        case HttpRequest(POST, _, _, _, _) =>
            // TODO parse body as json to Config
            // TODO initialize data processing
            // TODO await visualization response and send as response
            sender ! HttpResponse(entity = HttpEntity(`application/json`,
                "{\"test\": 1}"))

        case _:Http.ConnectionClosed =>
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
