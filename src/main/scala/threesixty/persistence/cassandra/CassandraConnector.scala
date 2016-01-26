package threesixty.persistence.cassandra

import com.websudos.phantom.connectors.{ContactPoints, KeySpace}

/**
  * Created by Stefan Cimander on 19.01.16.
  */
trait CassandraKeyspace {
    implicit val keySpace = KeySpace("threesixity")
}

object CassandraConnector extends CassandraKeyspace {
    val hosts = Seq("localhost")
    val keyspace = ContactPoints(hosts).keySpace("threesixty")
}
