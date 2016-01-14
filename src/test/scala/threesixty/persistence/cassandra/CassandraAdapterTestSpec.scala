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

    val timeframe = new Timeframe(new Timestamp(1452349667052L), new Timestamp(1452349687060L))
    val reliabilty = Reliability.User
    val resolution = Resolution.Low
    val scaling = Scaling.Ordinal
    val activityType = new ActivityType("Running")
    activityType.setDescription("Short run in the morning")
    val measurement = "Heart Rate"

    val metaData = new InputMetadata(timeframe, Reliability.User, Resolution.Low, Scaling.Ordinal, activityType)
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


           val either_data = cassandraAdapter.getDataSet(id)
            match {
                case Left(errormsg) => {print(errormsg)
                                        true should be (false)
                    //force test to collapse
                                            }

                case Right(x) => {var data = x

                    data.isInstanceOf[String] should be(false)
                    (data == null) should be (false)

                    data.id should be (id)
                    data.measurement should be (measurement)
                    data.metadata.reliability should be (reliabilty)
                    data.metadata.resolution should be (resolution)
                    data.metadata.scaling should be (scaling)
                    data.dataPoints should be (List(dataPoint))

                }
            }


        }
    }

}
