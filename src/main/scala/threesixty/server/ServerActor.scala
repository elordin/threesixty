package threesixty.server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging


object ServerActor {

    def props:Props = Props(new ServerActor )

}


class ServerActor extends Actor {
    val log = Logging(context.system, this)

    def receive = {
        case e => {
            println(e.toString)
            context.system.terminate
        }
    }

}
