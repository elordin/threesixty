package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.UUID

import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpec}
import org.scalatest.concurrent.ScalaFutures
import threesixty.data.Data.DoubleValue
import threesixty.data.{InputData, DataPoint}
import threesixty.data.metadata._

import scala.concurrent.Await
import scala.concurrent.duration._

import com.websudos.phantom.dsl._

/**
  * Created by Stefan Cimander on 20.01.16.
  */
class CassandraAdapterTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

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
    val msize = dataPoints.length
    val inputMetadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, msize)

    val inputDataSet = InputData(identifier.toString, measurement, dataPoints, inputMetadata)

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate().future(), Duration.Inf)
    }

    describe("inserting a new input data set") {
        it("should store and load the input data set correctly") {

            CassandraAdapter.insertData(inputDataSet)

            CassandraAdapter.getDataset(identifier.toString) match {
                case Right(resultInputDataSet) => resultInputDataSet should be(inputDataSet)
                    resultInputDataSet.metadata should be(inputDataSet.metadata)
                    resultInputDataSet.dataPoints should be(inputDataSet.dataPoints)
                case Left(message) => fail(message)
            }
        }
    }

    describe("inserting an existing input data set with already existing data points") {
        it("should not append additional data points to the existing input data set") {

            CassandraAdapter.insertData(inputDataSet)

            val firstExistingDataPoint = DataPoint(new Timestamp(1453227516719L), DoubleValue(130.3))
            val secondExistingDataPoint = DataPoint(new Timestamp(1453227568330L), DoubleValue(128.7))
            val newDataPoints = List(firstExistingDataPoint, secondExistingDataPoint)

            val newInputMetadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, 2)
            val newInputDataSet = new InputData(identifier.toString, measurement, newDataPoints, newInputMetadata)

            CassandraAdapter.getDataset(identifier.toString) match {
                case Right(resultInputDataSet) =>
                    resultInputDataSet should be(inputDataSet)
                case Left(message) => fail(message)
            }
        }
    }

    describe("inserting an existing input data set with a new data point") {
        it("should append the new data point to the existing set of data points") {

            CassandraAdapter.insertData(inputDataSet)

            val fifthDataPoint = DataPoint(new Timestamp(1453227510719L), DoubleValue(131.3))
            val newInputDataSet = inputDataSet.addNewDataPoints(List(fifthDataPoint))
            CassandraAdapter.insertData(newInputDataSet);

            CassandraAdapter.getDataset(identifier.toString) match {
                case Right(resultInputDataSet) =>
                    resultInputDataSet should be(newInputDataSet)
                case Left(message) => fail(message)
            }
        }
    }

    describe("inserting an existing input data set with partially existing data points") {
        it("should append the new data points to the existing set of data points") {

            CassandraAdapter.insertData(inputDataSet)

            val existingDataPoint = DataPoint(new Timestamp(1453227516719L), DoubleValue(130.3))
            val newDataPoint = DataPoint(new Timestamp(1453227510719L), DoubleValue(131.3))
            val newInputDataSet = inputDataSet.addNewDataPoints(List(existingDataPoint, newDataPoint))
            CassandraAdapter.insertData(newInputDataSet)

            CassandraAdapter.getDataset(identifier.toString) match {
                case Right(resultInputDataSet) =>
                    resultInputDataSet should be(newInputDataSet)
                case Left(message) => fail(message)
            }
        }
    }

    describe("querying a data set with data points for a given range of time") {
        it ("should load the data points within the given range") {

            CassandraAdapter.insertData(inputDataSet)

            val startTime = new Timestamp(1453227568330L)
            val endTime = new Timestamp(1453227593147L)

            CassandraAdapter.getDatasetInRange(identifier.toString, startTime, endTime) match {
                case Right(resultInputDataSet) =>
                    resultInputDataSet.dataPoints.length should be (2)
                case Left(message) => fail(message)
            }
        }
    }
}