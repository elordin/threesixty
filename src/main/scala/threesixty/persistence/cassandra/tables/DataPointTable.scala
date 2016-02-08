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

    object timeStamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Ascending
    object value extends DoubleColumn(this)
    object inputDataId extends UUIDColumn(this) with PartitionKey[UUID]

    def fromRow(row: Row): DataPoint = {
        val resultTimestamp = new Timestamp(timeStamp(row).getMillis)
        val resultValue = DoubleValue(value(row))

        DataPoint(resultTimestamp, resultValue)
    }
}

abstract class DataPoints extends DataPointsTable with RootConnector {

    /**
      * stores a given DataPoint in the database
      *
      * @param dataPoint   which dataPoint to store
      * @param inputDataId connect dataPoint to an InputData
      * @param identifier  (opt) give identifier with which the dataPoint is stored in the table
      * @return an awaitable future object
      */
    def store(dataPoint: DataPoint, inputDataId: UUID, identifier: UUID = UUID.randomUUID()): Future[ResultSet] = {
        val dateTime = new DateTime(dataPoint.timestamp.getTime);

        insert
            .value(_.timeStamp, dateTime)
            .value(_.inputDataId, inputDataId)
            .value(_.value, dataPoint.value.value)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

    def getDataPointsWithInputDataId(inputDataId: UUID): Future[Seq[DataPoint]] = {
        select.where(_.inputDataId eqs inputDataId).fetch()
    }

    def getDataPointsWithInputDataId(inputDataId: UUID, start: Timestamp, end: Timestamp)
    : Future[Seq[DataPoint]] = {
        select
            .where(_.inputDataId eqs inputDataId)
            .and(_.timeStamp gte new DateTime(start.getTime))
            .and(_.timeStamp lte new DateTime(end.getTime))
            .allowFiltering()
            .fetch()
    }

}
