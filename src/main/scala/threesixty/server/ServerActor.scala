package threesixty.server

import akka.actor.{Actor, ActorRef, Props}

object ServerActor {

    def props:Props = Props(new ServerActor )

}

class ServerActor extends Actor {

    def receive = {
        case _ => {}
    }

}
