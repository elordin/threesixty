package threesixty.persistence.cassandra

import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl
import threesixty.data.Data._
import threesixty.data.InputData
import threesixty.persistence.DatabaseAdapter
import threesixty.persistence.cassandra.tables.{InputDatasets, Timeframes, ActivityTypes, DataPoints}

/**
  * Created by Stefan Cimander on 14.01.16.
  */
class CassandraAdapter(val keyspace: KeySpaceDef) extends DatabaseImpl(keyspace) with DatabaseAdapter {

    object dataPoints extends DataPoints with keyspace.Connector
    object activityTypes extends ActivityTypes with keyspace.Connector
    object timeframes extends Timeframes with keyspace.Connector
    object inputDatasets extends InputDatasets with keyspace.Connector

    /**
      *  Retrieves a data set from the storage
      *
      *  @param id Id of the data to retrieve
      *  @return Either the data set (Left) or Left(errormsg) on error
      */
    def getDataset(id:Identifier):Either[String, InputData] = ???

    /**
      *  Appends data to a dataset of give id
      *
      *  @param data Data to insert into the database
      *  @return Either Right(uuid), new id of inserted data, or Left(errormsg) on error
      */
    def insertData(data:InputData):Either[String, Identifier] = ???

    /**
      *  Appends data to a data set of give id
      *
      *  @param data Data to insert into the database
      *  @param id Id of existing data set to append to
      *  @return Either Right(id), id of appended data, or Left(errormsg) on error
      */
    def appendData(data:InputData, id:Identifier):Either[String, Identifier] = ???

    /**
      *  Attempts to append data to a data set of give id.
      *  If the id does not exist, a new data set is created.
      *
      *  @param data Data to insert into the database
      *  @param id Id of data set to append to
      *  @return Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
      */
    def appendOrInsertData(data:InputData, id:Identifier):Either[String, Identifier] = ???

}

object CassandraAdapter extends CassandraAdapter(CassandraConnector.keyspace)




