package threesixty.persistence.cassandra

import com.typesafe.config.{ConfigException, ConfigFactory, Config}
import com.websudos.phantom.connectors.{ContactPoints, KeySpace}

/**
  * Created by Stefan Cimander on 19.01.16.
  */
trait CassandraKeyspace {
    implicit val keySpace = KeySpace("threesixity")
}

object CassandraConnector extends CassandraKeyspace {
    val config: Config = ConfigFactory.load

    @throws[ConfigException]("if config doesn't contain database.address") // TODO
    val dbAddress: String =
        config.getString("database.address")
    @throws[ConfigException]("if config doesn't contain database.keyspace") // TODO
    val dbKeyspace: String =
        config.getString("database.keyspace")

    // val hosts = Seq("137.250.170.136")
    // val hosts = Seq("localhost")
    val hosts = Seq(dbAddress)

    val keyspace = ContactPoints(hosts).keySpace(dbKeyspace)
}
