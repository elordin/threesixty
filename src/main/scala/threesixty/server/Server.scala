package threesixty.server

import akka.actor.{ActorSystem, ActorRef}
import akka.io.IO

import spray.can.Http

import com.typesafe.config.{Config, ConfigFactory, ConfigException}

/**
 *  Main application; creates server with configuration from config files.
 *  @author Thomas Weber
 */
object Server extends App {

    val config = ConfigFactory.load

    implicit lazy val system = ActorSystem()

    val server:ActorRef = system.actorOf(ServerActor.props, "server")

    val port:Int = try {
            config.getInt("server.port")
        } catch {
            case _:ConfigException => 8080
        }

    val interface:String = try {
            config.getString("server.interface")
        } catch {
            case _:ConfigException => "localhost"
        }

    IO(Http) ! Http.Bind(server, interface = interface, port = port)
}
