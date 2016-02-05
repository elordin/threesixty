package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.datastax.driver.core.ConsistencyLevel
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.connectors.RootConnector
import com.websudos.phantom.dsl._
import threesixty.data.Data._
import threesixty.data.metadata._
import threesixty.persistence.cassandra.CassandraAdapter

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputMetadataTable extends CassandraTable[InputMetadataSets, CompleteInputMetadata] {

    object identifier extends UUIDColumn(this) with PartitionKey[UUID]
    object reliability extends StringColumn(this)
    object resolution extends StringColumn(this)
    object scaling extends StringColumn(this)
    object timeframeId extends UUIDColumn(this)
    object activityTypeId extends UUIDColumn(this)
    object size extends IntColumn(this)

    def fromRow(row: Row): CompleteInputMetadata = {
        val resultResolution = Resolution.withName(resolution(row))
        val resultReliability = Reliability.withName(reliability(row))
        val resultScaling = Scaling.withName(scaling(row))
        val resultSize = size(row)

        val resultTimeframe = Await.result(CassandraAdapter.timeframes
            .getTimeframeByIdentifier(timeframeId(row)), Duration.Inf) match {
            case Some(timeframe) => timeframe
            case None => new Timeframe(new Timestamp(0), new Timestamp(0))
        }

        val resultActivityType = Await.result(CassandraAdapter.activityTypes
            .getById(activityTypeId(row)), Duration.Inf) match {
            case Some(activityType) => activityType
            case None => new ActivityType("Not defined")
        }

        CompleteInputMetadata(resultTimeframe, resultReliability, resultResolution, resultScaling, resultActivityType, resultSize)
    }
}

abstract class InputMetadataSets extends InputMetadataTable with RootConnector {

    /** stores a given Metadata object in the database
      *
      * @param inputMetadata the metadata to store
      * @param identifier    (opt) give identifier with which the metadata is stored in the table
      * @return an awaitable future object
      */
    def store(inputMetadata: CompleteInputMetadata, identifier: UUID = UUID.randomUUID): Future[ResultSet] = {
        val timeframeId = UUID.randomUUID()
        val activityTypeId = UUID.randomUUID()

        Await.result(CassandraAdapter.timeframes.store(inputMetadata.timeframe, timeframeId), Duration.Inf)
        Await.result(CassandraAdapter.activityTypes.store(inputMetadata.activityType, activityTypeId), Duration.Inf)

        insert().value(_.identifier, identifier)
            .value(_.reliability, inputMetadata.reliability.toString)
            .value(_.resolution, inputMetadata.resolution.toString)
            .value(_.scaling, inputMetadata.scaling.toString)
            .value(_.activityTypeId, activityTypeId)
            .value(_.timeframeId, timeframeId)
            .value(_.size, inputMetadata.size)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

    def getInputMetadataByIdentifier(identifier: UUID): Future[Option[CompleteInputMetadata]] = {
        select.where(_.identifier eqs identifier).one()
    }

    def getTimeframeId(identifier: UUID): Future[Option[UUID]] = {
        select(_.timeframeId).where(_.identifier eqs identifier).one()

    }

    def updateSizeForIdentifier(identifier: UUID, newSize: Int): Future[ResultSet] = {
        update
            .where(_.identifier eqs identifier)
            .modify(_.size setTo newSize)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

}
