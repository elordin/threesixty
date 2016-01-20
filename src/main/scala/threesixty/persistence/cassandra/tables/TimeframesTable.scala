package threesixty.persistence.cassandra.tables

import java.util.UUID

import com.datastax.driver.core.ConsistencyLevel
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.DateTimeColumn
import com.websudos.phantom.connectors.RootConnector
import com.websudos.phantom.dsl._
import com.websudos.phantom.keys.PartitionKey
import org.joda.time.DateTime
import threesixty.data.Data.Timestamp
import threesixty.data.metadata.Timeframe

import scala.concurrent.Future

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class TimeframesTable extends CassandraTable[Timeframes, Timeframe] {

    object identifier extends UUIDColumn(this) with PartitionKey[UUID]
    object startTime extends DateTimeColumn(this)
    object endTime extends DateTimeColumn(this)

    def fromRow(row: Row): Timeframe = {
        val resultStartTime = new Timestamp(startTime(row).getMillis)
        val resultEndTime = new Timestamp(endTime(row).getMillis)

        Timeframe(resultStartTime, resultEndTime)
    }

}

abstract class Timeframes extends TimeframesTable with RootConnector {

    def store(timeframe: Timeframe, identifier: UUID = UUID.randomUUID()): Future[ResultSet] = {
        val startTime = new DateTime(timeframe.start.getTime())
        val endTime = new DateTime(timeframe.end.getTime)
        insert.value(_.identifier, identifier)
            .value(_.startTime, startTime)
            .value(_.endTime, endTime)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

    def getTimeframeByIdentifier(identifier: UUID): Future[Option[Timeframe]] = {
        select.where(_.identifier eqs identifier).one()
    }

}
