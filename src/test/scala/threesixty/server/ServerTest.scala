package threesixty.server

import akka.actor.{ActorSystem, Actor, Props}
import akka.testkit.{TestActors, TestKit, TestProbe}
import akka.io.Tcp.{Closed, Aborted, ConfirmedClosed, PeerClosed, ErrorClosed}

import spray.http._
import HttpMethods._

import scala.concurrent.duration._

import org.scalatest.{FunSpecLike, BeforeAndAfterAll}

class ServerTestSpec(_system:ActorSystem) extends TestKit(_system)
        with FunSpecLike with BeforeAndAfterAll {

    def this() = this(ActorSystem("ServerTestSpec"))


    describe("An APIHandler") {
        describe("when receiving any Http GET Request") {
            val apiHandler = system.actorOf(APIHandler.props)


            it("must respond with a HttpResponse") {
                apiHandler ! HttpRequest(method = GET)
                expectMsgClass[HttpResponse](1.seconds, classOf[HttpResponse])
            }
        }

        describe("closing behavior") {
            describe("when receiving a Closed") {
                val apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch apiHandler
                apiHandler ! Closed
                it("must terminate") {
                    probe.expectTerminated(apiHandler)
                }
            }
            describe("when receiving a Aborted") {
                val apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch apiHandler
                apiHandler ! Aborted
                it("must terminate") {
                    probe.expectTerminated(apiHandler)
                }
            }
            describe("when receiving a ConfirmedClosed") {
                val apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch apiHandler
                apiHandler ! ConfirmedClosed
                it("must terminate") {
                    probe.expectTerminated(apiHandler)
                }
            }
            describe("when receiving a PeerClosed") {
                val apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch apiHandler
                apiHandler ! PeerClosed
                it("must terminate") {
                    probe.expectTerminated(apiHandler)
                }
            }
            describe("when receiving a ErrorClosed") {
                val apiHandler = system.actorOf(APIHandler.props)
                val probe = TestProbe()
                probe watch apiHandler
                apiHandler ! ErrorClosed("cause reasons")
                it("must terminate") {
                    probe.expectTerminated(apiHandler)
                }
            }
        }
    }

    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
}
