package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import threesixty.data.{InputData, InputDataSkeleton}
import threesixty.persistence.cassandra.CassandraAdapter

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputDataTable extends CassandraTable[InputDatasets, InputDataSkeleton] {

    object identifier extends UUIDColumn(this) with PartitionKey[UUID]
    object measurement extends StringColumn(this)
    object inputMetadataId extends UUIDColumn(this)

    implicit def ordered: Ordering[Timestamp] = new Ordering[Timestamp] {
        def compare(x: Timestamp, y: Timestamp): Int = x compareTo y
    }


    def fromRow(row: Row): InputDataSkeleton = {
        val resultIdentifier = identifier(row).toString
        val resultMeasurement = measurement(row)
        val resultInputMetadata = Await.result(CassandraAdapter.inputMetadataSets
            .getInputMetadataByIdentifier(inputMetadataId(row)), Duration.Inf) match {
            case Some(inputMetadata) => inputMetadata
            case None => throw new NoSuchElementException(s"No metadata found for $resultIdentifier")
        }
        new InputDataSkeleton(resultIdentifier, resultMeasurement, resultInputMetadata)
    }
}

abstract class InputDatasets extends InputDataTable with RootConnector {

    def store(inputData: InputData): Future[ResultSet] = {
        val inputMetadataId = UUID.randomUUID()

        Await.result(CassandraAdapter.inputMetadataSets.store(inputData.metadata, inputMetadataId), Duration.Inf)

        for (dataPoint <- inputData.dataPoints) {
            Await.result(CassandraAdapter.dataPoints
                .store(dataPoint, UUID.fromString(inputData.id)), Duration.Inf)
        }

        insert.value(_.identifier, UUID.fromString(inputData.id))
            .value(_.measurement, inputData.measurement)
            .value(_.inputMetadataId, inputMetadataId)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

    /**
      * calls fromRow method*/
    def getInputDataByIdentifier(identifier: UUID): Future[Option[InputData]] = {

        getInputDataSkeletonByIdentifier(identifier).map(_.map({
            skeleton: InputDataSkeleton  => {
                skeleton fill Await.result(CassandraAdapter.dataPoints
                    .getDataPointsWithInputDataId(UUID.fromString(skeleton.id)), Duration.Inf).toList.sortBy(_.timestamp.getTime)
            }
        }))


    }

    def getMetadataID(identifier: UUID):  Future[Option[UUID]] ={
        select(_.inputMetadataId).where(_.identifier eqs identifier).one()
    }

    def getInputDataSkeletonByIdentifier(identifier: UUID): Future[Option[InputDataSkeleton]] =
        select.where(_.identifier eqs identifier).one()
}
