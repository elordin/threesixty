package threesixty.persistence.cassandra

import java.util.UUID
import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl
import threesixty.data.Data._
import threesixty.data.metadata.{Timeframe, CompleteInputMetadata}
import threesixty.data.{DataPoint, InputData, InputDataSubset, InputDataSkeleton}
import threesixty.persistence.DatabaseAdapter
import threesixty.persistence.cassandra.tables._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Stefan Cimander on 14.01.16.
  */
class CassandraAdapter(val keyspace: KeySpaceDef) extends DatabaseImpl(keyspace) with DatabaseAdapter {

    object dataPoints extends DataPoints with keyspace.Connector
    object activityTypes extends ActivityTypes with keyspace.Connector
    object timeframes extends Timeframes with keyspace.Connector
    object inputDatasets extends InputDatasets with keyspace.Connector
    object inputMetadataSets extends InputMetadataSets with keyspace.Connector

    /**
      * Retrieves a data set from the storage
      *
      * @param id Id of the data to retrieve
      * @return Either the data set (Left) or Left(errormsg) on error
      */
    def getDataset(id: Identifier): Either[String, InputData] = {
        try {
            Await.result(CassandraAdapter.inputDatasets
                .getInputDataByIdentifier(UUID.fromString(id)), Duration.Inf) match {
                case Some(result) => Right(result)
                case None => Left("Failed to load data set with identifier: " + id)
            }
        } catch {
            case e: NoSuchElementException => Left(e.getMessage)
        }
    }

    /**
      * Inserts data set into the database
      *
      * @param data Data to insert into the database
      * @return Either Right(uuid), new id of inserted data, or Left(errormsg) on error
      */
    def insertData(data: InputData): Either[String, Identifier] = {
        val dataSetAlreadyExists = getDataset(data.id)
        dataSetAlreadyExists match {
            case Left(_) => insert(data)
            case Right(_) => append(data)
        }
    }



    /*
     * Inserts new data set into the database
     */
    private def insert(data: InputData): Either[String, Identifier] = {
        Await.result(CassandraAdapter.inputDatasets.store(data), Duration.Inf)
        Right(data.id)
    }

    /*
     * Appends the data points which are not already stored to the data set
     */
    private def append(data: InputData): Either[String, Identifier] = {
        val pointsToAppend = pointsNotAlreadyStored(data.dataPoints, UUID.fromString(data.id))
        if (pointsToAppend.isEmpty) {
            Left("All data points are already stored in the database")
        }
        else {
            pointsToAppend.foreach(CassandraAdapter.dataPoints.store(_, UUID.fromString((data.id))))

            val metadataID = Await.result(CassandraAdapter.inputDatasets.getMetadataID(UUID.fromString(data.id)), Duration.Inf)
            val inputMetadata = Await.result(CassandraAdapter.inputMetadataSets.getInputMetadataByIdentifier(metadataID.get), Duration.Inf)

            val newSize = inputMetadata.get.size + pointsToAppend.length
            updateSizeForMetadataIdentifier(metadataID.get, newSize)

            val allPoints = data.dataPoints ++ pointsToAppend
            val newStart = allPoints.minBy(_.timestamp.getTime).timestamp
            val newEnd = allPoints.maxBy(_.timestamp.getTime).timestamp

            updateTimeframeForMetadataIdentifier(metadataID.get, newStart, newEnd)

            return Right(data.id)
        }
    }

    /*
     * Removes the points that are already stored from the data points
     */
    private def pointsNotAlreadyStored(pointsToCheck: List[DataPoint], id: UUID): List[DataPoint] = {
        val pointsAlreadyStored = Await.result(CassandraAdapter.dataPoints.getDataPointsWithInputDataId(id), Duration.Inf)
        pointsToCheck.diff(pointsAlreadyStored)
    }

    /*
     * Updates the size for the input metadata entry
     */
    private def updateSizeForMetadataIdentifier(identifier: UUID, newSize: Int) = {
        Await.result(CassandraAdapter.inputMetadataSets.updateSizeForIdentifier(identifier, newSize), Duration.Inf)
    }

    /*
     * Updates the time frame for input metadata entry
     */
    private def updateTimeframeForMetadataIdentifier(identifier: UUID, newStart: Timestamp, newEnd: Timestamp) = {
        val timeframeID = Await.result(CassandraAdapter.inputMetadataSets.getTimeframeId(identifier), Duration.Inf)
        Await.result(CassandraAdapter.timeframes.updateTimeframe(timeframeID.get, newStart, newEnd), Duration.Inf)
    }

    /**
      * Retrieves a data set for a specific time range from the storage
      *
      * @param identifier Identifier of data to retreive
      * @param from       The start timestamp of the range
      * @param to         The end timestamp of the range
      * @return           Either the data set (Left) or an error message (Right)
      */
    def getDatasetInRange(identifier: Identifier, from: Timestamp, to: Timestamp): Either[String, InputDataSubset] = ???



    /**
      *  Gets only the metadata for a datset with given ID.
      *
      *  @param identifier ID of data whose metadata is requested
      *  @return Some[CompleteInputMetadata] of the requested dataset or None on error
      */
    def getMetadata(identifier: Identifier) : Option[CompleteInputMetadata] = {
        Await.result(CassandraAdapter.inputMetadataSets.getInputMetadataByIdentifier(UUID.fromString(identifier)), Duration.Inf)
    }

    def getSkeleton(identifier: Identifier): Either[String, InputDataSkeleton] =
        try {
            Await.result(CassandraAdapter.inputDatasets
                .getInputDataSkeletonByIdentifier(UUID.fromString(identifier)), Duration.Inf) match {
                case Some(result: InputDataSkeleton) => Right(result)
                case None => Left("Failed to load data set with identifier: " + identifier)
            }
        } catch {
            case e: NoSuchElementException => Left(e.getMessage)
        }


}

object CassandraAdapter extends CassandraAdapter(CassandraConnector.keyspace)




