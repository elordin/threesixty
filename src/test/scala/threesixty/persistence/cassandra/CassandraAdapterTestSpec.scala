package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.UUID

import org.scalatest.{FunSpec, Matchers}
import threesixty.data.metadata._
import threesixty.data.{InputData, DataPoint}
import threesixty.data.Implicits._


/**
  * Created by Stefan Cimander on 09.01.16.
  */
class CassandraAdapterTestSpec extends FunSpec with Matchers{

    val timeframe = new Timeframe(new Timestamp(1452349667052L), new Timestamp(1452349687060L))
    val reliabilty = Reliability.User
    val resolution = Resolution.Low
    val scaling = Scaling.Ordinal
    val activityType = new ActivityType("Running")
    activityType.setDescription("Short run in the morning")
    val measurement = "Heart Rate"

    val metaData = new CompleteInputMetadata(timeframe, Reliability.User, Resolution.Low, Scaling.Ordinal, activityType)
    val dataPoint = new DataPoint(1452343334, 200)

    val id = UUID.randomUUID().toString
    val inputData = new InputData(id, measurement, List(dataPoint), metaData)

    val uri = CassandraConnectionUri("cassandra://localhost:9042/test")
    val cassandraAdapter = new CassandraAdapter(uri)



    describe("Inserting input data into the Cassandra database") {
        it("should contain the input data") {

            cassandraAdapter.insertData(inputData)
            cassandraAdapter.containsDataPointWithId(id) should be (true)
        }
    }
    describe("Reading from the database"){
        it("should read the inserted metadata related to a given input"){

            val result = cassandraAdapter.getDataset(id)

            assert(result.isRight)

            val data = result.right.get

            (data) should not be (null)

            data.id should be (id)
            data.measurement should be (measurement)
            data.metadata.reliability should be (reliabilty)
            data.metadata.resolution should be (resolution)
            data.metadata.scaling should be (scaling)
            data.data should be (List(dataPoint))



        }
    }

}
