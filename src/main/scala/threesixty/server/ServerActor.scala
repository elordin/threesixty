package threesixty.server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import spray.can.Http


object ServerActor {
    def props:Props = Props(new ServerActor )
}


/**
 *  Primary dispatcher; receives HTTP Requests and dispatches
 *  them to newly created APIHandlers.
 */
class ServerActor extends Actor {
    val log = Logging(context.system, this)

    def receive = {
        case _:Http.Connected =>
            // created and attach new handler for API calls
            val peer = sender
            val apiHandler:ActorRef = context.actorOf(APIHandler.props)
            peer ! Http.Register(apiHandler)

        case msg => log.error("Unknown message: " + msg)

    }

}
