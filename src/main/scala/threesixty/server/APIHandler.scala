package threesixty.server

import threesixty.processor.Processor
import threesixty.visualizer.Visualizer
import threesixty.engine.{VisualizationResponse, VisualizationEngine}
import threesixty.persistence.FakeDatabaseAdapter
import threesixty.visualizer.visualizations.PieChart.PieChartConfig
import threesixty.visualizer.visualizations._
import threesixty.algorithms.interpolation.LinearInterpolation


import threesixty.data.Data.Identifier
import threesixty.data.InputData

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

    @throws[ConfigException]("if config doesn't contain database.uri") // TODO
    val dbURI: String =
        config.getString("database.uri")


    lazy val engine: Engine = VisualizationEngine(
        new Processor
            with LinearInterpolation.Mixin,
        new Visualizer
            with LineChart.Mixin
            with PieChart.Mixin,
        FakeDatabaseAdapter
    )

    def props: Props = Props(new APIHandler)
}


/**
 *  Reads the HTTP requests from a client and passes them to the engine. Sends responses.
 */
class APIHandler extends Actor {

    val log = Logging(context.system, this)

    override def postRestart(reason: Throwable): Unit = {
        log.error("APIHandler restarting: " + reason.toString)
    }

    def receive = {
        case request@HttpRequest(POST, _, _, _: HttpEntity.NonEmpty, _) =>
            val peer = sender

            val processingFuture: Future[HttpResponse] = Future {
                APIHandler.engine.processRequest(request).toHttpResponse
            }

            processingFuture onSuccess {
                case response: HttpResponse => peer ! response
            }

            processingFuture onFailure {
                case t: Throwable =>
                    peer ! HttpResponse(
                        status = StatusCodes.InternalServerError,
                        entity = HttpEntity(`application/json`, s"""{ "error": "${t.getMessage}" }"""),
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
            log.error("Unknown message: " + msg)
            sender ! HttpResponse(
                status = StatusCodes.MethodNotAllowed,
                entity = HttpEntity(`application/json`, """{ "error": "Unknown message." }"""),
                headers = List(`Access-Control-Allow-Origin`(AllOrigins))
            )
    }

}
