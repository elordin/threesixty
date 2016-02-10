package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.{UUID, Calendar}

import com.websudos.phantom.dsl._

import play.api.libs.json._

import org.scalatest.{BeforeAndAfterAll, FunSpec}
import threesixty.data.Data.ValueType
import threesixty.data.metadata.{ActivityType, CompleteInputMetadata, InputMetadata, Timeframe}
import threesixty.data.{DataPoint, InputData}
import threesixty.persistence.ExampleDataGenerator

import scala.concurrent.Await
import scala.concurrent.duration._

class TestDataForDatabase extends FunSpec with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    var generator = new ExampleDataGenerator()
    var now = new Timestamp(Calendar.getInstance().getTime.getTime)


    describe("generating heart rate data for one month") {
        it ("should generate the heart rate data and store it into the database") {

            val date = new DateTime(2016, 1, 1, 0, 0)
            val inputData = generator.exampleHeartRate(48, 225, 2678400, new Timestamp(date.getMillis))

            CassandraAdapter.insertData(inputData)
        }
    }
    
    def dataPointsToJson(dataPoints: List[DataPoint]): JsValue = {
        Json.toJson(dataPoints.map { dataPoint =>
            Json.obj(
                "timestamp" -> dataPoint.timestamp,
                "value" -> valueTypeToJson(dataPoint.value)
            )}
        )
    }

    def valueTypeToJson(valueType: ValueType): JsValue = {
        Json.obj(
            "type" -> "double",
            "value" -> valueType.value
        )
    }

    def activityTypeToJson(activityType: ActivityType): JsValue = {
        Json.obj(
            "name" -> activityType.name
        )
    }

    def timeframeToJson(timeframe: Timeframe): JsValue = {
        Json.obj(
            "start" -> timeframe.start,
            "end" -> timeframe.end
        )
    }

    def metadataToJson(metadata: CompleteInputMetadata): JsValue = {
        Json.obj(
            "timeframe" -> timeframeToJson(metadata.timeframe),
            "reliability" -> metadata.reliability.toString,
            "resolution" -> metadata.resolution.toString,
            "scaling" -> metadata.scaling.toString,
            "activityType" -> activityTypeToJson(metadata.activityType),
            "size" -> metadata.size
        )
    }

    def inputDataToJson(inputData: InputData): JsValue = {
        Json.obj(
            "id" -> inputData.id,
            "measurement" -> inputData.measurement,
            "dataPoints" -> dataPointsToJson(inputData.dataPoints),
            "metadata" -> metadataToJson(inputData.metadata)
        )
    }

    /*
    describe("generating test data for the presentation day") {
        it ("should just create a sample data set for one day, that can be loaded by the app") {

            val date = new DateTime(2015, 12, 21, 1, 1)
            val inputData = generator.generateStepsForDay(date,"23551219-404e-42a7-bc95-95accb8affe5")

            println(inputDataToJson(inputData))
        }
    }
    */



}
