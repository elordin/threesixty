package threesixty.persistence.cassandra

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import org.scalatest.{Matchers, FunSpec}

class ConnectionAndQueryTestSpec extends FunSpec with Matchers{

    describe("Connecting and querying a Cassandra database") {
        it ("should connect and get sample data from query") {
            val uri = CassandraConnectionUri("cassandra://localhost:9042/test")
            val session = CassandraConnector.createSessionAndInitKeyspace(uri)

            session.execute("CREATE TABLE IF NOT EXISTS things (id int, name text, PRIMARY KEY (id))")
            session.execute("INSERT INTO things (id, name) VALUES (1, 'foo');")

            val selectStmt = ("SELECT name FROM things WHERE id = 1 LIMIT 1")
            /*
            val selectStmt = select().column("name").
                from("things").
                where(QueryBuilder.eq("id", 1)).
                limit(1)
            */
            val resultSet = session.execute(selectStmt)
            val row = resultSet.one()
            row.getString("name") should be ("foo")
            session.execute("DROP TABLE things;")
        }
    }
}