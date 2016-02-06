package threesixty.persistence.cassandra

import com.websudos.phantom.connectors.{ContactPoints, KeySpace}
import threesixty.server.APIHandler

/**
  * Created by Stefan Cimander on 19.01.16.
  */
trait CassandraKeyspace {
    implicit val keySpace = KeySpace("threesixity")
}

object CassandraConnector extends CassandraKeyspace {
    // val hosts = Seq("137.250.170.136")
    // val hosts = Seq("localhost")
    val hosts = Seq(APIHandler.dbAddress)
    val dbKeyspace = APIHandler.dbKeyspace

    val keyspace = ContactPoints(hosts).keySpace(dbKeyspace)
}
