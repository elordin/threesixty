package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import threesixty.data.metadata._
import threesixty.persistence.cassandra.{CassandraAdapter, CassandraConnector}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputMetadataTableTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), 5.seconds)
    }

    describe("Inserting an input metadata set") {
        it("should store and load the input metadata set correctly") {

            val identifier = UUID.randomUUID()
            val timeframe = Timeframe(new Timestamp(1453223865112L), new Timestamp(1453223912299L))
            val activityType = ActivityType("Sleeping")
            val resolution = Resolution.Middle
            val reliability = Reliability.Device
            val scaling = Scaling.Ordinal
            val size = 5

            val inputMetadta = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, size)

            Await.result(CassandraAdapter.inputMetadataSets.store(inputMetadta, identifier), Duration.Inf)

            whenReady(CassandraAdapter.inputMetadataSets.getInputMetadataByIdentifier(identifier), timeout(Duration.Inf)) {
                case Some(result) => result should be (inputMetadta)
                    result.reliability should be (reliability)
                    result.resolution should be (resolution)
                    result.scaling should be (scaling)
                    result.timeframe should be (timeframe)
                    result.activityType should be (activityType)
                    result.size should be (size)
                case None => fail("Did not receive an input metadata result from the database.")
            }
        }
    }

}
