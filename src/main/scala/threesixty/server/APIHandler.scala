package threesixty.server

import threesixty.processor.Processor
import threesixty.visualizer.Visualizer
import threesixty.engine.VisualizationEngine
import threesixty.persistence.cassandra.{CassandraAdapter, CassandraConnectionUri}

import threesixty.visualizer.visualizations._

import threesixty.data.Data.Identifier
import threesixty.data.InputData

import akka.actor.{Actor, Props}
import akka.event.Logging
import spray.http.{HttpMethods, MediaTypes, HttpEntity, HttpResponse, HttpRequest, StatusCodes}
import spray.can.Http

import HttpMethods.{GET, POST}
import MediaTypes.`application/json`

import com.typesafe.config.{Config, ConfigFactory, ConfigException}


object APIHandler {

    val config: Config = ConfigFactory.load
    // throws ConfigException
    val dbURI: String = config.getString("database.uri")


    lazy val engine = VisualizationEngine(
        new Processor {},

        new Visualizer
            with LineChartConfig.Info
            with BarChartConfig.Info
            with PieChartConfig.Info,
        new CassandraAdapter(dbURI)
    )

    def props: Props = Props(new APIHandler )
}

/**
 *  Reads the HTTP request and dispatches it to an EngineActor
 */
class APIHandler extends Actor {
    val log = Logging(context.system, this)

    override def postRestart(reason: Throwable): Unit = {
        log.error(reason.toString)
    }

    def receive = {
        case request@HttpRequest(POST, _, _, _: HttpEntity.NonEmpty, _) =>
            var response = APIHandler.engine.processRequest(request)
            sender ! response.toHttpResponse

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
