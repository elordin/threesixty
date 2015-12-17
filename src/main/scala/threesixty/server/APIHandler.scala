package threesixty.server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import spray.http._
import spray.can.Http


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

    println("APIHandler created")

    def receive = {
        case HttpRequest(method, uri, headers, entity, protocol) =>
            // send request
            log.info("Receive http request")
            context stop self

        case Http.ConfirmedClose =>
            log.info("Shutting down")
            context stop self

        case msg => log.error("Unknown message: " + msg)
    }

}
