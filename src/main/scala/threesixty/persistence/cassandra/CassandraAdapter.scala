package threesixty.persistence.cassandra

import java.util.UUID
import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl
import threesixty.data.Data._
import threesixty.data.{DataPoint, InputData}
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
      *  Retrieves a data set from the storage
      *
      *  @param id Id of the data to retrieve
      *  @return Either the data set (Left) or Left(errormsg) on error
      */
    def getDataset(id:Identifier):Either[String, InputData] = {
        Await.result(CassandraAdapter.inputDatasets
            .getInputDataByIdentifier(UUID.fromString(id)), Duration.Inf) match {
            case Some(result) => Right(result)
            case None => Left("Failed to load data set with identifier: " + id)
        }
    }

    /**
      *  Appends data to a dataset of given id
      *
      *  @param data Data to insert into the database
      *  @return Either Right(uuid), new id of inserted data, or Left(errormsg) on error
      */
    def insertData(data:InputData):Either[String, Identifier] = {
        Await.result(CassandraAdapter.inputDatasets.store(data), Duration.Inf)
        Right(data.id)
    }


    /**
      *  Attempts to append data to a data set of given id.
      *  If the id does not exist, a new data set is created.
      *
      *  @param data Data to insert into the database
      *  @param id Id of data set to append to
      *  @return Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
      */
    def appendOrInsertData(data:InputData):Either[String, Identifier] = {

        val testIfExisting = getDataset(data.id)
        testIfExisting match {
            case Left(_) => insertData(data)
            case Right(_) => appendData(data)
            case _ => Left("Something went wrong during appendOrInsert of new Data (id: " + data.id +")")
        }

    }




    /**
      *  Appends data to a data set of given id
      *
      *  @param data Data to insert into the database
      *  @param id Id of existing data set to append to
      *  @return Either Right(id), id of appended data, or Left(errormsg) on error
      */
    def appendData(data:InputData):Either[String, Identifier] = {
        val dataId = UUID.fromString(data.id)
        val points = data.dataPoints
        val cleanedPoints = cleanPoints(points, dataId)

        if  (cleanedPoints.isEmpty)
            {Left("All Datapoints were already stored in the Database")}
        else {
            try {
                cleanedPoints.foreach(CassandraAdapter.dataPoints.store(_, dataId))
                updateMetadata(data, points ++ cleanedPoints)
            }
            catch {
                case e: Exception => Left("Error in appending data to existing Data. " + e.toString)
            }
        }

    }

    /**
     * helper Method that ensures only new points are appended to the InputData*/
      private  def cleanPoints(points : List[DataPoint], id : UUID): List[DataPoint] ={

        val existingPoints = Await.result(CassandraAdapter.dataPoints.getDataPointsWithInputDataId(id), Duration.Inf)

        points.diff(existingPoints)
    }





    /**
      * private Method that is called after appending new datapoints to InputData
      * Updates the Timeframe of the param InputData
      */
   private def updateMetadata(inputData: InputData, points: List[DataPoint]) : Either[String, Identifier] = {
        val dataset = getDataset(inputData.id) match {
            case Right(data) => data
        }

         val min = points.minBy(_.timestamp.getTime).timestamp
         val max = points.maxBy(_.timestamp.getTime).timestamp

        try {
            //GOTO MetaDataTable  -> get TimeframeID
            val metaId = Await.result(CassandraAdapter.inputDatasets.getMetadataID(UUID.fromString(inputData.id)), Duration.Inf)
            //GOTO Timeframe with ID
            val timeframe_Id = Await.result(CassandraAdapter.inputMetadataSets.getTimeframeId(metaId.get), Duration.Inf)
            //-> update start(min), end(max)
            Await.result(CassandraAdapter.timeframes.updateTimeframe(timeframe_Id.get, min, max), Duration.Inf)

        Right(inputData.id)
        }
        catch{
            case e: Exception => Left("problem when updating the timeframe for new dataPoints \n " + e.toString)
            case _ => Left("problem when updating the timeframe for new dataPoints. - no more detailed information available")
        }

    }

}

object CassandraAdapter extends CassandraAdapter(CassandraConnector.keyspace)




