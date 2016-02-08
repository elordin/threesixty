package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.{UUID, Calendar}

import com.websudos.phantom.dsl._

import org.scalatest.{BeforeAndAfterAll, FunSpec}
import threesixty.persistence.ExampleDataGenerator

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Stefan Cimander on 01.02.16.
  */
class TestDataForDatabase extends FunSpec with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(), 30.seconds)
    }

    var generator = new ExampleDataGenerator()
    var now = new Timestamp(Calendar.getInstance().getTime.getTime)


    describe("generating test data for the steps made in current month") {
        it ("should generate the step data and store it into the database") {

            val stepIdentifier = UUID.randomUUID().toString
            val date = new DateTime(2016, 1, 31, 1, 1)

            val inputData = generator.generateStepsForMonth(date, "23551219-404e-42a7-bc95-95accb8affe5")

            CassandraAdapter.insertData(inputData)
        }
    }

}
