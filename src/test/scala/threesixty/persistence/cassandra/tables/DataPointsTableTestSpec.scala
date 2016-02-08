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

    val inputDataId = UUID.randomUUID()

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), Duration.Inf)
    }

    describe("Asking for data points with a specific inputDataId") {
        it("should load an empty sequence if there are no appropriate data points") {

            whenReady(CassandraAdapter.dataPoints.getDataPointsWithInputDataId(inputDataId), timeout(Duration.Inf)) {
                case results => results should be (Seq())
            }
        }
    }

    describe("Inserting multiple data points") {
        it("should store and reload the data points correctly") {

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

    describe("Querying data points within a range of two time stamps") {
        it("it should load only data points that are in the given range") {

            var firstTimestamp = new Timestamp(1454939471285L)
            var secondTimestamp = new Timestamp(1454939489746L)
            var thirdTimestamp = new Timestamp(1454939516045L)

            var firstDataPoint = DataPoint(firstTimestamp, 11.11)
            var secondDataPoint = DataPoint(secondTimestamp, 22.22)
            var thirdDataPoint = DataPoint(thirdTimestamp, 33.33)

            Await.result(CassandraAdapter.dataPoints.store(firstDataPoint, inputDataId), Duration.Inf)
            Await.result(CassandraAdapter.dataPoints.store(secondDataPoint, inputDataId), Duration.Inf)
            Await.result(CassandraAdapter.dataPoints.store(thirdDataPoint, inputDataId), Duration.Inf)

            whenReady(CassandraAdapter.dataPoints.getDataPointsWithInputDataId
                (inputDataId, secondTimestamp, thirdTimestamp)) {
                case results =>
                    results.size should be (2)
                    results.contains(firstDataPoint) should be (false)
                    results.contains(secondDataPoint) should be (true)
                    results.contains(thirdDataPoint) should be (true)

            }
        }
    }

}
