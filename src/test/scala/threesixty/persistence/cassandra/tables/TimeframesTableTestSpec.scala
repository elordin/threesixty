package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import threesixty.data.metadata.Timeframe
import threesixty.persistence.cassandra.{CassandraAdapter, CassandraConnector}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class TimeframesTableTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), Duration.Inf)
    }

    override def afterAll(): Unit = {
        super.afterAll()
        Await.result(CassandraAdapter.autotruncate().future(), Duration.Inf)
    }

    describe("Inserting a new time frame") {
        it("should store and load the time frame correctly") {

            val identifier = UUID.randomUUID()
            val startTime = new Timestamp(1453219812355L)
            val endTime = new Timestamp(1453219830747L)
            val timeframe = new Timeframe(startTime, endTime)

            Await.result(CassandraAdapter.timeframes.store(timeframe, identifier), Duration.Inf)

            whenReady(CassandraAdapter.timeframes.getTimeframeByIdentifier(identifier), timeout(Duration.Inf)) {
                case Some(result) => result should be(timeframe)
                    result.start should be(startTime)
                    result.end should be(endTime)
                case None => fail("Received no result from the database.")
            }
        }
    }
}