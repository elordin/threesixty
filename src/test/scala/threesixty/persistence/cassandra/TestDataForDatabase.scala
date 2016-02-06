package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.Calendar

import com.websudos.phantom.dsl._

import org.scalatest.{BeforeAndAfterAll, FunSpec}
import threesixty.persistence.ExampleDataGenerator

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by stefancimander on 01.02.16.
  */
class TestDataForDatabase extends FunSpec with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), 30.seconds)
    }

    var generator = new ExampleDataGenerator()
    var now = new Timestamp(Calendar.getInstance().getTime.getTime)
    var data = generator.exampleHeartRate(48, 225, 100, now)

    /*
     * This method is only used to fill the database with some test data
     */


    describe("Generating test data and inserting them into database") {
        it ("Generates heart rate data") {

            CassandraAdapter.insertData(data)
        }
    }

    describe("Retreiving data from the database") {
        it ("should send the requested input data set") {

            CassandraAdapter.getDataset(data.id) match {
                case Right(data) => print(data)
                case Left(msg) => fail(msg)
            }

        }
    }

}
