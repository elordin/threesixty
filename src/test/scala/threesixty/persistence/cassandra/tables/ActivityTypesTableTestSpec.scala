package threesixty.persistence.cassandra.tables

import java.util.UUID

import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import threesixty.data.metadata.ActivityType
import threesixty.persistence.cassandra.{CassandraAdapter, CassandraConnector}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class ActivityTypesTableTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    override def beforeAll(): Unit = {
        super.beforeAll()
        Await.result(CassandraAdapter.autocreate.future(),Duration.Inf)
    }

    describe("Inserting a new activity type without description") {
        it("should store and load the activity name correctly") {

            val identifier = UUID.randomUUID()
            val activityType = ActivityType("Swimming")

            Await.result(CassandraAdapter.activityTypes.store(activityType, identifier), Duration.Inf)

            whenReady(CassandraAdapter.activityTypes.getById(identifier), timeout(Duration.Inf)) {
                case Some(result) => result should be (activityType)
                    result.name should be ("Swimming")
                    result.description should be (null)
                case None => fail("Received no result from the database.")
            }
        }
    }

    describe("Inserting a new activity type with a description") {
        it("should store and load the activity name and description correctly") {

            val identifier = UUID.randomUUID()
            val activityType = ActivityType("Running")
            activityType.setDescription("Fast Run in the Evening")

            Await.result(CassandraAdapter.activityTypes.store(activityType, identifier), Duration.Inf)

            whenReady(CassandraAdapter.activityTypes.getById(identifier), timeout(Duration.Inf)) {
                case Some(result) => result should be (activityType)
                    result.name should be ("Running")
                    result.description should be ("Fast Run in the Evening")
                case None => fail("Received no result from the database.")
            }
        }
    }

}
