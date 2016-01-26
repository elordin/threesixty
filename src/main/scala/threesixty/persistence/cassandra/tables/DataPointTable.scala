package threesixty.persistence.cassandra.tables

import java.sql.{Timestamp}
import java.util.UUID

import com.datastax.driver.core.ConsistencyLevel
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.connectors.RootConnector
import com.websudos.phantom.dsl._
import org.joda.time.DateTime
import threesixty.data.Data.DoubleValue
import threesixty.data.DataPoint

import scala.concurrent.Future

/**
  * Created by Stefan Cimander on 18.01.16.
  */
class DataPointsTable extends CassandraTable[DataPoints, DataPoint] {

    object identifier extends UUIDColumn(this) with PrimaryKey[UUID]
    object timestamp extends DateTimeColumn(this)
    object value extends DoubleColumn(this)
    object inputDataId extends UUIDColumn(this) with PartitionKey[UUID]

    def fromRow(row: Row): DataPoint = {
        val resultTimestamp = new Timestamp(timestamp(row).getMillis)
        val resultValue = DoubleValue(value(row))

        DataPoint(resultTimestamp, resultValue)
    }
}

abstract class DataPoints extends DataPointsTable with RootConnector {

    def store(dataPoint: DataPoint, inputDataId: UUID, identifier: UUID = UUID.randomUUID()): Future[ResultSet] = {
        val dateTime = new DateTime(dataPoint.timestamp.getTime());
        insert.value(_.identifier, identifier)
            .value(_.timestamp, dateTime)
            .value(_.inputDataId, inputDataId)
            .value(_.value, dataPoint.value.value)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

    def getDataPointWithIdentifier(identifier: UUID): Future[Option[DataPoint]] = {
        select.where(_.identifier eqs identifier).allowFiltering().one()
    }

    def getDataPointsWithInputDataId(inputDataId: UUID): Future[Seq[DataPoint]] = {
        select.where(_.inputDataId eqs inputDataId).fetch()
    }

}
