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
        Await.result(CassandraAdapter.autocreate.future(), 5.seconds)
    }

    override def afterAll(): Unit = {
        super.afterAll()
        Await.result(CassandraAdapter.autotruncate.future(), 5.seconds)
    }

    describe("Inserting a input data set") {
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
            val inputMetadta = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType)

            val inputDataSet = InputData(identifier.toString, measurement, dataPoints, inputMetadta)

            CassandraAdapter.insertData(inputDataSet) should be (Right(identifier.toString))
            CassandraAdapter.getDataset(identifier.toString) should be (Right(inputDataSet))
        }
    }


}
