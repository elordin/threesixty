package threesixty.server

import akka.actor.{ActorSystem, Actor, Props}
import akka.testkit.{TestActors, TestKit, TestProbe, ImplicitSender}
import akka.io.Tcp.{Closed, Aborted, ConfirmedClosed, PeerClosed, ErrorClosed}

import spray.http._
import HttpMethods._
import MediaTypes._

import scala.concurrent.duration._

import org.scalatest.{FunSpecLike, BeforeAndAfterAll}

class ServerTestSpec(_system:ActorSystem) extends TestKit(_system)
        with FunSpecLike with BeforeAndAfterAll with ImplicitSender {

    def this() = this(ActorSystem("ServerTestSpec"))


    describe("An APIHandler") {
        val apiHandler = system.actorOf(APIHandler.props)
        describe("when receiving a HttpRequest using POST") {

            it("must respond with a HttpResponse") {
                apiHandler ! HttpRequest(method = POST)
                expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
            }
        }

        describe("when receiving a HttpRequest using GET") {
            it("must respond with a HttpResponse with status code 405 Method Not Allowed") {
                apiHandler ! HttpRequest(method = GET)
                val response = expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
                assert(response.status.intValue == 405)
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