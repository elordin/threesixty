package threesixty.persistence.cassandra

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpec}

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputDataTableTestSpec extends FunSpec with Matchers with ScalaFutures
    with BeforeAndAfterAll with CassandraConnector.keyspace.Connector {

    describe("Test") {
        it("should work") {

            // TODO: Implement test
        }
    }

}
