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


    describe("Inserting input data into a Cassandra database") {
        it("should work") {

            var timestamp = 1452343334
            var value = 33.3
            val dataPoint = new DataPoint(timestamp, value)



            val activityType = new ActivityType("Running")
            activityType.setDescription("Mountain Running")




            var startTime = new Timestamp(1452349667052L)
            var endTime = new Timestamp(1452349667060L)
            var timeframe = new Timeframe(startTime, endTime)


            var reliabilty = Reliability.User
            var resolution = Resolution.Low
            var scaling = Scaling.Ordinal



            val metaData = new InputMetadata(timeframe, reliabilty, resolution, scaling, activityType)

            val id = UUID.randomUUID().toString
            var inputData = new InputData(id, "Velocity", List(dataPoint), metaData)

            CassandraAdapter.insertData(inputData)
        }

    }

}
