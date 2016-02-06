package threesixty.server

import threesixty.persistence.cassandra.CassandraAdapter
import threesixty.persistence.FakeDatabaseAdapter
import threesixty.ProcessingMethods.Accumulation.Accumulation
import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.ProcessingMethods.TimeSelection.TimeSelection
import threesixty.processor.Processor
import threesixty.visualizer.Visualizer
import threesixty.engine.{VisualizationEngine, Engine}
import threesixty.ProcessingMethods.interpolation.LinearInterpolation
import threesixty.visualizer.visualizations._

import akka.actor.{Actor, Props}
import akka.event.Logging
import spray.http.{HttpMethods, MediaTypes, HttpEntity, HttpResponse, HttpRequest, StatusCodes, HttpHeaders, AllOrigins}
import spray.can.Http

import HttpMethods.{GET, POST}
import MediaTypes.`application/json`
import HttpHeaders.`Access-Control-Allow-Origin`

import com.typesafe.config.{Config, ConfigFactory, ConfigException}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object APIHandler {

    val config: Config = ConfigFactory.load

    val debug: Boolean = try {
        config.getBoolean("debug")
    } catch {
        case _:ConfigException => false
    }

    lazy val engine: Engine = VisualizationEngine using
        new Processor   with LinearInterpolation.Mixin
                        with Aggregation.Mixin
                        with Accumulation.Mixin
                        with TimeSelection.Mixin and new Visualizer
            with lineChart.Mixin
            with pieChart.Mixin
            with barChart.Mixin
            with scatterChart.Mixin and CassandraAdapter

    def props: Props = Props(new APIHandler)
}


/**
 *  Reads the HTTP requests from a client and passes them to the engine. Sends responses.
 *
 *  @author Thomas Weber
 */
class APIHandler extends Actor {

    val log = Logging(context.system, this)

    override def postRestart(reason: Throwable): Unit = {
        log.error("APIHandler restarting: " + reason.toString)
    }

    def receive = {
        case HttpRequest(POST, _, _, body: HttpEntity.NonEmpty, _) =>
            val peer = sender
            val processingFuture: Future[HttpResponse] = Future {
                APIHandler.engine.processRequest(body.asString).toHttpResponse
            }

            processingFuture onSuccess {
                case response: HttpResponse => peer ! response
            }

            processingFuture onFailure {
                case t: Throwable =>
                    peer ! HttpResponse(
                        status = StatusCodes.InternalServerError,
                        entity = HttpEntity(`application/json`,
                            s"""{ "error": "${if (APIHandler.debug) t.getMessage else "Internal Server Error" }" }"""),
                        headers = List(`Access-Control-Allow-Origin`(AllOrigins))
                    )
            }

        case HttpRequest(POST, _, _, HttpEntity.Empty, _) =>
            sender ! HttpResponse(
                status = StatusCodes.MethodNotAllowed,
                entity = HttpEntity(`application/json`, """{ "error": "Empty request body." }"""),
                headers = List(`Access-Control-Allow-Origin`(AllOrigins))
            )

        case HttpRequest(GET, _, _, _, _) =>
            sender ! HttpResponse(
                status = StatusCodes.MethodNotAllowed,
                entity = HttpEntity(`application/json`, """{ "error": "GET not allowed" }"""),
                headers = List(`Access-Control-Allow-Origin`(AllOrigins))
            )

        case _: Http.ConnectionClosed =>
            context stop self

        case msg =>
            log.info("Unknown message: " + msg)
            sender ! HttpResponse(
                status = StatusCodes.MethodNotAllowed,
                entity = HttpEntity(`application/json`, """{ "error": "Unknown message." }"""),
                headers = List(`Access-Control-Allow-Origin`(AllOrigins))
            )
    }

}
