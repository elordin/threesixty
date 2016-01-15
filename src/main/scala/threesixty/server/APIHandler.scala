package threesixty.server

import threesixty.processor.Processor
import threesixty.visualizer.Visualizer
import threesixty.engine.Engine
import threesixty.persistence.DatabaseAdapter

import threesixty.visualizer.visualizations._

import threesixty.data.Data.Identifier
import threesixty.data.InputData

import akka.actor.{Actor, Props}
import akka.event.Logging
import spray.http.{HttpMethods, MediaTypes, HttpEntity, HttpResponse, HttpRequest, StatusCodes}
import spray.can.Http

import HttpMethods.{GET, POST}
import MediaTypes.`application/json`


object APIHandler {
    val engine = Engine(
        new Processor {},

        new Visualizer
            with LineChartConfig.Info
            with BarChartConfig.Info
            with PieChartConfig.Info,

        new DatabaseAdapter {
            def getDataset(id:Identifier):Either[String, InputData] = ???
            def insertData(data:InputData):Either[String, Identifier] = ???
            def appendData(data:InputData, id:Identifier):Either[String, Identifier] = ???
            def appendOrInsertData(data:InputData, id:Identifier):Either[String, Identifier] = ???
        }
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
        case HttpRequest(POST, _, _, body: HttpEntity.NonEmpty, _) =>
            var response = APIHandler.engine.processRequest(body.asString)
            sender ! response.toHttpResponse

        case HttpRequest(GET, _, _, _, _) =>
            import threesixty.data.{ProcessedData, TaggedDataPoint}
            import threesixty.data.Data.Timestamp

            val data = ProcessedData("data", List(
                TaggedDataPoint(new Timestamp(0), 10, Set()),
                TaggedDataPoint(new Timestamp(1), 20, Set()),
                TaggedDataPoint(new Timestamp(2), 15, Set()),
                TaggedDataPoint(new Timestamp(3), 25, Set())
            ))
            val visualization = new LineChartConfig.LineChart(LineChartConfig(Set("data"), 768, 1024, title="Test"), Set(data))
            sender ! threesixty.engine.VisualizationResponse(visualization).toHttpResponse

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
