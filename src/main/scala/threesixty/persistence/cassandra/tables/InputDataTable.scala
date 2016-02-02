package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._
import threesixty.persistence.cassandra.CassandraAdapter

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputDataTable extends CassandraTable[InputDatasets, InputData] {

    object identifier extends UUIDColumn(this) with PartitionKey[UUID]
    object measurement extends StringColumn(this)
    object inputMetadataId extends UUIDColumn(this)

    implicit def ordered: Ordering[Timestamp] = new Ordering[Timestamp] {
        def compare(x: Timestamp, y: Timestamp): Int = x compareTo y
    }
/**
  * this method is just for internal use.
  * it is called via getXYZ-methods in child classes*/
    def fromRow(row: Row): InputData = {
        val resultIdentifier = identifier(row).toString
        val resultMeasurement = measurement(row)

        val dataPointsSequence = Await.result(CassandraAdapter.dataPoints
            .getDataPointsWithInputDataId(UUID.fromString(resultIdentifier)), Duration.Inf)

        var resultDataPoints = List[DataPoint]()
        for (dataPoint <- dataPointsSequence) resultDataPoints ++=  List(dataPoint)

        resultDataPoints = resultDataPoints.sortBy(_.timestamp)

        val resultInputMetadata = Await.result(CassandraAdapter.inputMetadataSets
            .getInputMetadataByIdentifier(inputMetadataId(row)), Duration.Inf) match {
            case Some(inputMetadata) => inputMetadata
            case None => null
        }

        InputData(resultIdentifier, resultMeasurement, resultDataPoints, resultInputMetadata)
    }
}

abstract class InputDatasets extends InputDataTable with RootConnector {

    /**
      * stores a given InputData in the database
      * @param inputData which InputData to store
      * @return returns an awaitable future object
      *
      * acts like controller who delegates storage of associated Metadata, too*/
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
        select.where(_.identifier eqs identifier).one()
    }

    def getMetadataID(identifier: UUID):  Future[Option[UUID]] ={
        select(_.inputMetadataId).where(_.identifier eqs identifier).one()

    }
}
