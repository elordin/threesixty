package threesixty.server

import threesixty.processor.Processor
import threesixty.visualizer.Visualizer
import threesixty.engine.VisualizationEngine
import threesixty.persistence.FakeDatabaseAdapter

import threesixty.visualizer.visualizations._
import threesixty.algorithms.interpolation.LinearInterpolation


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
    // TODO: throws ConfigException
    val dbURI: String = config.getString("database.uri")


    lazy val engine = VisualizationEngine(
        new Processor
            with LinearInterpolation.Info,
        new Visualizer
            with LineChartConfig.Info
            with BarChartConfig.Info
            with PieChartConfig.Info,
        FakeDatabaseAdapter
    )

    def props: Props = Props(new APIHandler )
}

/**
 *  Reads the HTTP request and dispatches it to an EngineActor
 */
class APIHandler extends Actor {

    val log = Logging(context.system, this)

    override def postRestart(reason: Throwable): Unit = {
        log.error(reason.toString) // TODO
    }

    def receive = {
        // TODO
        case request@HttpRequest(POST, _, _, _: HttpEntity.NonEmpty, _) =>
            var response = APIHandler.engine.processRequest(request)
            sender ! response.toHttpResponse
        // TODO
        case HttpRequest(GET, _, _, _, _) =>
            var response = APIHandler.engine.processRequest("""{"type": "visualization", "visualization": { "type": "linechart", "args": "" }, "data": ["data1", "data2", "data3"] }""")
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
