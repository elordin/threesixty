package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import threesixty.data.Data.DoubleValue
import threesixty.data.metadata._
import threesixty.data.{DataPoint, InputData}
import threesixty.persistence.cassandra.{CassandraAdapter, CassandraConnector}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputDataTableTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), Duration.Inf)
    }

    describe("Inserting an input data set") {
        it("should store and load the input data set correctly") {

            val identifier = UUID.randomUUID()
            val measurement = "Heart Rate"

            val firstDataPoint = DataPoint(new Timestamp(1453227516719L), DoubleValue(130.3))
            val secondDataPoint = DataPoint(new Timestamp(1453227568330L), DoubleValue(128.7))
            val thirdDataPoint = DataPoint(new Timestamp(1453227593147L), DoubleValue(129.1))
            val fourthDataPoint = DataPoint(new Timestamp(1453227615119L), DoubleValue(129.5))
            val dataPoints = List(firstDataPoint, secondDataPoint, thirdDataPoint, fourthDataPoint)

            val timeframe = Timeframe(new Timestamp(1453227383043L), new Timestamp(1453227461703L))
            val activityType = ActivityType("Walking")
            activityType.setDescription("Long walk with my dogs")
            val resolution = Resolution.High
            val reliability = Reliability.Device
            val scaling = Scaling.Ordinal
            val size = dataPoints.length
            val inputMetadta = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, size)

            val inputDataSet = InputData(identifier.toString, measurement, dataPoints, inputMetadta)

            Await.result(CassandraAdapter.inputDatasets.store(inputDataSet), Duration.Inf)

            whenReady(CassandraAdapter.inputDatasets.getInputDataByIdentifier(identifier), timeout(Duration.Inf)) {
                case Some(result) => result should be (inputDataSet)
                    result.measurement should be (measurement)
                    result.dataPoints should be (dataPoints)
                    result.metadata should be (inputMetadta)
                case None => fail("Did not receive an input data result from the database.")
            }
        }
    }

}
