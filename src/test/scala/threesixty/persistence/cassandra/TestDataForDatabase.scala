package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.Calendar

import org.scalatest.FunSpec
import threesixty.persistence.ExampleDataGenerator

/**
  * Created by stefancimander on 01.02.16.
  */
class TestDataForDatabase extends FunSpec {

    var generator = new ExampleDataGenerator()
    var now = new Timestamp(Calendar.getInstance().getTime.getTime)
    var databaseAdabter = CassandraAdapter
    var data = generator.exampleHeartRate(48, 225, 100, now)

    /*
     * This method is only used to fill the database with some test data
     *
    describe("Generating test data and inserting them into database") {
        it ("Generates heart rate data") {

            databaseAdabter.insertData(data)
        }
    }
    */
}
