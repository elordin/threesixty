package threesixty.persistence.cassandra

import java.util.UUID

import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl
import threesixty.data.Data._
import threesixty.data.InputData
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
      *  Appends data to a data set of given id
      *
      *  @param data Data to insert into the database
      *  @param id Id of existing data set to append to
      *  @return Either Right(id), id of appended data, or Left(errormsg) on error
      */
    def appendData(data:InputData):Either[String, Identifier] = ???

    /**
      *  Attempts to append data to a data set of given id.
      *  If the id does not exist, a new data set is created.
      *
      *  @param data Data to insert into the database
      *  @param id Id of data set to append to
      *  @return Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
      */
    def appendOrInsertData(data:InputData):Either[String, Identifier] = ???

}

object CassandraAdapter extends CassandraAdapter(CassandraConnector.keyspace)




