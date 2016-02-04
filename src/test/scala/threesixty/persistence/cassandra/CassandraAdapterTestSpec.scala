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

    override def afterAll(): Unit = {
        super.afterAll()
        Await.result(CassandraAdapter.autotruncate().future(), Duration.Inf)
    }


    describe("inserting a new input data set") {
        it ("should store and load the input data set correctly") {

            CassandraAdapter.insertData(inputDataSet)

            CassandraAdapter.getDataset(identifier.toString) match {
                case Right(resultInputDataSet) => resultInputDataSet should be (inputDataSet)
                    resultInputDataSet.metadata should be (inputDataSet.metadata)
                    resultInputDataSet.dataPoints should be (inputDataSet.dataPoints)
                case Left(message) => fail(message)
            }
        }
    }

    describe("inserting an existing input data set with a new data point") {
        it ("should append the new data point to the existing set of data points") {

            CassandraAdapter.insertData(inputDataSet)

            val fifthDataPoint = DataPoint(new Timestamp(1453227510719L), DoubleValue(131.3))
            val newInputDataSet = inputDataSet.addNewDataPoints(List(fifthDataPoint))
            CassandraAdapter.insertData(newInputDataSet);

            CassandraAdapter.getDataset(identifier.toString) match {
                case Right(resultInputDataSet) =>
                    // resultInputDataSet should be (newInputDataSet)
                    resultInputDataSet.dataPoints should be (newInputDataSet.dataPoints)
                case Left(message) => fail(message)
            }
        }
    }










/*
    describe("Insert or append newly received data") {

        it("should append data point to the correct data set") {

            CassandraAdapter.insertData(inputDataSet) should be (Right(identifier.toString))

            val fifthDataPoint = DataPoint(new Timestamp(1453227510719L), DoubleValue(190.3))
            val newInputDataSet = inputDataSet.addNewDataPoints(List(fifthDataPoint))

            CassandraAdapter.insertData(newInputDataSet) should be (Right(identifier.toString))
        }

    }

*/
/*

    describe("InsertOrAppend newly received Data") {

        it("should insert data if it has not been in the database yet") {
            val newDataId = UUID.randomUUID().toString
            val newData = InputData(newDataId, measurement, dataPoints, inputMetadata)

            CassandraAdapter.insertData(newData) should be(Right(newDataId))
            CassandraAdapter.getDataset(newDataId) should be(Right(newData))
        }



        it("should append datapoints to dataset if dataset is already in the DB ") {
            val prevPoints = dataPoints
            val newPoint = DataPoint(new Timestamp(1453227510719L), DoubleValue(190.3))

            val dataWithNewPoint = InputData(inputDataSet.id.toString, measurement, List(newPoint), inputMetadata)

            CassandraAdapter.insertData(inputDataSet)

            CassandraAdapter.insertData(dataWithNewPoint) should be(Right(inputDataSet.id.toString))

            // CassandraAdapter.appendOrInsertData(dataWithNewPoint) should be(Left("All Datapoints were already stored in the Database"))

            /*
            val DataWithMorePoints = InputData(inputDataSet.id.toString, measurement, prevPoints ++ List(newPoint), inputMetadata)
            CassandraAdapter.appendOrInsertData(DataWithMorePoints) should be(Left("All Datapoints were already stored in the Database"))
            */


        }


    } /*
        it("should update the metadata of the expanded dataset"){
            val id = inputDataSet.id
            val originalTimeframe = inputDataSet.metadata.timeframe

            //fill database
            CassandraAdapter.appendOrInsertData(inputDataSet)

            //add a new point that causes the timestamp to get updated
            val newPoint = DataPoint(new Timestamp(1000000000000L), DoubleValue(0.003)) //-> forces update of Timeframe
            val DataNewPoint = InputData(id.toString, measurement, List(newPoint), inputMetadata)
            CassandraAdapter.appendOrInsertData(DataNewPoint) should be (Right(inputDataSet.id.toString)) //-> insert worked

            // check whether Timeframe got changed after insertion of that point
            var queriedTimeframe = CassandraAdapter.getDataset(id) match {
                    case Right(dataset) => dataset.metadata.timeframe
                    case Left(_) => fail()
                }
            assert(!queriedTimeframe.equals(originalTimeframe))
        }

        it("should get the metadata and length separately"){
            val id = inputDataSet.id
            CassandraAdapter.appendOrInsertData(inputDataSet)

            assert(inputMetadata equals CassandraAdapter.getMetadata(id))
            assert(CassandraAdapter.getDatapointsLength(id) == Right(4))
        }

    }



    */

    */
}