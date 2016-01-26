package threesixty.server

import akka.actor.{ActorSystem, Actor, Props}
import akka.testkit.{TestActors, TestKit, TestProbe, ImplicitSender}
import akka.io.Tcp.{Closed, Aborted, ConfirmedClosed, PeerClosed, ErrorClosed}

import spray.http._
import HttpMethods._
import MediaTypes._
import HttpHeaders._

import scala.concurrent.duration._

import org.scalatest.{FunSpecLike, BeforeAndAfterAll}

class ServerTestSpec(_system:ActorSystem) extends TestKit(_system)
        with FunSpecLike with BeforeAndAfterAll with ImplicitSender {

    def this() = this(ActorSystem("ServerTestSpec"))


    describe("An APIHandler") {
        val apiHandler = system.actorOf(APIHandler.props)
        describe("when receiving a HttpRequest using POST") {
            it("must respond with a HttpResponse with non-empty body") {
                apiHandler ! HttpRequest(method = POST, entity = HttpEntity(`application/json`, "{}"))
                expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
            }

            describe("if thre request had an empty body") {
                it("must respond with an error") {
                    apiHandler ! HttpRequest(method = POST, entity = HttpEntity.Empty)
                    val response = expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
                    assertResult(response) {
                        HttpResponse(
                            status = StatusCodes.MethodNotAllowed,
                            entity = HttpEntity(`application/json`, """{ "error": "Empty request body." }"""),
                            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
                        )
                    }
                }
            }
        }

        describe("when receiving a HttpRequest using GET") {
            it("must respond with a HttpResponse with status code 405 Method Not Allowed") {
                apiHandler ! HttpRequest(method = GET)
                val response = expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
                assert(response.status.intValue == 405)
            }
        }

        describe("when receiving a message that is not a HttpRequest") {
            describe("e.g. a None") {
                it("should respond with an error") {
                    apiHandler ! None
                    val response = expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
                    assertResult(response) {
                        HttpResponse(
                            status = StatusCodes.MethodNotAllowed,
                            entity = HttpEntity(`application/json`, """{ "error": "Unknown message." }"""),
                            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
                        )
                    }
                }
            }

            describe("e.g. an Integer") {
                it("should respond with an error") {
                    apiHandler ! 5
                    val response = expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
                    assertResult(response) {
                        HttpResponse(
                            status = StatusCodes.MethodNotAllowed,
                            entity = HttpEntity(`application/json`, """{ "error": "Unknown message." }"""),
                            headers = List(`Access-Control-Allow-Origin`(AllOrigins))
                        )
                    }
                }
            }
        }

        // Since each of the following tries to terminate the APIHandler,
        // each one creates a local new one it can then terminate.
        describe("closing behavior") {
            describe("when receiving a Closed") {
                val _apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch _apiHandler
                _apiHandler ! Closed
                it("must terminate") {
                    probe.expectTerminated(_apiHandler)
                }
            }
            describe("when receiving a Aborted") {
                val _apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch _apiHandler
                _apiHandler ! Aborted
                it("must terminate") {
                    probe.expectTerminated(_apiHandler)
                }
            }
            describe("when receiving a ConfirmedClosed") {
                val _apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch _apiHandler
                _apiHandler ! ConfirmedClosed
                it("must terminate") {
                    probe.expectTerminated(_apiHandler)
                }
            }
            describe("when receiving a PeerClosed") {
                val _apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch _apiHandler
                _apiHandler ! PeerClosed
                it("must terminate") {
                    probe.expectTerminated(_apiHandler)
                }
            }
            describe("when receiving a ErrorClosed") {
                val _apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch _apiHandler
                _apiHandler ! ErrorClosed("cause reasons")
                it("must terminate") {
                    probe.expectTerminated(_apiHandler)
                }
            }
        }
    }

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
}
