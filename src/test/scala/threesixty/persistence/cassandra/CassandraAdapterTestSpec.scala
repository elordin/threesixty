package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.UUID

import org.scalatest.{FunSpec, Matchers}
import threesixty.data.metadata.InputMetadata
import threesixty.data.{InputData, DataPoint}
import threesixty.metadata._


/**
  * Created by Stefan Cimander on 09.01.16.
  */
class CassandraAdapterTestSpec extends FunSpec with Matchers{

    val timeframe = new Timeframe(new Timestamp(1452349667052L), new Timestamp(1452349667060L))
    val reliabilty = Reliability.User
    val resolution = Resolution.Low
    val scaling = Scaling.Ordinal
    val activityType = new ActivityType("Running")
    activityType.setDescription("Short run in the morning")

    val metaData = new InputMetadata(timeframe, Reliability.User, Resolution.Low, Scaling.Ordinal, activityType)
    val dataPoint = new DataPoint(1452343334, 200)

    val id = UUID.randomUUID().toString
    val inputData = new InputData(id, "Heart Rate", List(dataPoint), metaData)


    describe("Inserting input data into the Cassandra database") {
        it("should contain the input data") {

            val uri = CassandraConnectionUri("cassandra://localhost:9042/test")
            val cassandraAdapter = new CassandraAdapter(uri)

            cassandraAdapter.insertData(inputData)
            cassandraAdapter.containsDataPointWithId(id) should be (true)
        }
    }

}
