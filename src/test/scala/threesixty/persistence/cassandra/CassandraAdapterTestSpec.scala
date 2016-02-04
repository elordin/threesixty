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

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), 30.seconds)
    }

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

    describe("Inserting a input data set") {
        it("should store and load the input data set correctly") {

            CassandraAdapter.insertData(inputDataSet) should be (Right(identifier.toString))
            CassandraAdapter.getDataset(identifier.toString) should be (Right(inputDataSet))
        }
    }

    describe("InsertOrAppend newly received Data"){

        it("should insert data if it has not been in the database yet"){
            val newDataId = UUID.randomUUID().toString
            val newData = InputData(newDataId, measurement, dataPoints, inputMetadata)

            CassandraAdapter.appendOrInsertData(newData) should be (Right(newDataId))
            CassandraAdapter.getDataset(newDataId) should be (Right(newData))
        }

        it("should append datapoints to dataset if dataset is already in the DB "){
            val prevPoints = dataPoints
            val newPoint = DataPoint(new Timestamp(1453227510719L), DoubleValue(190.3))
            val DataNewPoint = InputData(inputDataSet.id.toString, measurement, List(newPoint), inputMetadata)

            //Load some (irrelevant) data into Db
            CassandraAdapter.insertData(inputDataSet)
            //add a new datapoint
            CassandraAdapter.appendOrInsertData(DataNewPoint) should be (Right(inputDataSet.id.toString))
            //Do it a second time and get message that there are no new points to add
            CassandraAdapter.appendOrInsertData(DataNewPoint) should be (Left("All Datapoints were already stored in the Database"))
            //Do it with newPoint + previously added points -> same message
            val DataWithMorePoints = InputData(inputDataSet.id.toString, measurement, prevPoints ++ List(newPoint), inputMetadata)
            CassandraAdapter.appendOrInsertData(DataWithMorePoints) should be (Left("All Datapoints were already stored in the Database"))
        }

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


}
