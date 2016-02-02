package threesixty.server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import spray.can.Http


object ServerActor {
    def props: Props = Props(new ServerActor)
}

/**
 *  Primary dispatcher; receives HTTP Requests and dispatches
 *  them to newly created APIHandlers.
 */
class ServerActor extends Actor {
    // Maintain a list of distributed processing actors
    // Pass the LFU to every new APIHandler

    val log = Logging(context.system, this)

    def receive = {
        case Http.Connected(remoteAddr, _) =>
            // created and attach new handler for API calls
            log.info("Creating API handler for %s.".format(remoteAddr.toString))
            val peer = sender // explicitly saving the peer since it may be overwriten during actorOf
            val apiHandler: ActorRef = context.actorOf(APIHandler.props)
            peer ! Http.Register(apiHandler)

        case msg => log.error("Unknown message: " + msg)
    }

}
