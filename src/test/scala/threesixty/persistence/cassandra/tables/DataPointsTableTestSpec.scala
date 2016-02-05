package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import threesixty.data.Data.DoubleValue
import threesixty.data.DataPoint
import threesixty.persistence.cassandra.{CassandraAdapter, CassandraConnector}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Stefan Cimander on 14.01.16.
  */
class DataPointsTableTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), Duration.Inf)
    }

    override def afterAll(): Unit = {
        super.afterAll()
        Await.result(CassandraAdapter.autotruncate().future(), Duration.Inf)
    }

    describe("Asking for data points with a specific inputDataId") {
        it("should load an empty sequence if there are no appropriate data points") {

            val inputDataId = UUID.randomUUID()

            whenReady(CassandraAdapter.dataPoints.getDataPointsWithInputDataId(inputDataId), timeout(Duration.Inf)) {
                case results => results should be (Seq())
            }
        }
    }

    describe("Inserting a single data point") {
        it("should store and reload the data point correctly") {

            val identifier = UUID.randomUUID()
            val timestamp = new Timestamp(1453162975367L)
            val value = new DoubleValue(64.64)
            val inputDataId = UUID.randomUUID()
            val dataPoint = DataPoint(timestamp, value)

            Await.result(CassandraAdapter.dataPoints.store(dataPoint, inputDataId, identifier), Duration.Inf)

            whenReady(CassandraAdapter.dataPoints.getDataPointWithIdentifier(identifier), timeout(Duration.Inf)) {
                case Some(result) => result should be (dataPoint)
                case None => fail("Received no result from the database.")
            }
        }
    }

    describe("Inserting multiple data points") {
        it("should store and reload the data points correctly") {
            val inputDataId = UUID.randomUUID()

            var timestamp = new Timestamp(1453156139450L)
            var value = new DoubleValue(42.42)
            val firstDataPoint = DataPoint(timestamp, value)

            Await.result(CassandraAdapter.dataPoints.store(firstDataPoint, inputDataId), Duration.Inf)

            timestamp = new Timestamp(1453160644872L)
            value = new DoubleValue(53.53)
            val secondDataPoint = DataPoint(timestamp, value)

            Await.result(CassandraAdapter.dataPoints.store(secondDataPoint, inputDataId), Duration.Inf)

            whenReady(CassandraAdapter.dataPoints.getDataPointsWithInputDataId(inputDataId), timeout(Duration.Inf)) {
                case results =>
                    results.contains(firstDataPoint) should be (true)
                    results.contains(secondDataPoint) should be (true)
            }
        }
    }

}
