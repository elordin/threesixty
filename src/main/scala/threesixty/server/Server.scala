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
    val DEFAULT_PORT = 8080
    val DEFAULT_INTERFACE = "127.0.0.1"

    val config = ConfigFactory.load

    implicit lazy val system = ActorSystem()

    val port: Int = try {
        config.getInt("server.port")
    } catch {
        case _: ConfigException => DEFAULT_PORT
    }

    val interface: String = try {
        config.getString("server.interface")
    } catch {
        case _: ConfigException => DEFAULT_INTERFACE
    }

    val server: ActorRef = system.actorOf(ServerActor.props, "server")

    IO(Http) ! Http.Bind(server, interface = interface, port = port)
}
