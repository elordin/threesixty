package threesixty.persistence.cassandra

import java.util.UUID

import threesixty.data.metadata.InputMetadata
import threesixty.data.{DataPoint, InputData}
import threesixty.metadata.Reliability._
import threesixty.metadata.Resolution._
import threesixty.metadata.Scaling._
import threesixty.metadata.{ActivityType, Timeframe}
import threesixty.persistence.DatabaseAdapter

import scala.Option

/**
  * Created by Stefan Cimander on 09.01.16.
  */
class CassandraAdapter(uri: CassandraConnectionUri) extends DatabaseAdapter {

    val session = CassandraConnector.createSessionAndInitKeyspace(uri)

    createTables()

    def createTables() = {
        session.execute("CREATE TABLE IF NOT EXISTS ActivityType " +
            "(id text, name text, description text, PRIMARY KEY(id))")

        session.execute("CREATE TABLE IF NOT EXISTS Timeframe " +
            "(id text, startTime timestamp, endTime timestamp, PRIMARY KEY(id))")

        session.execute("CREATE TABLE IF NOT EXISTS InputMetaData " +
            "(id text, timeframeID text, scaling text, resolution text, reliability text, activityTypeID text, PRIMARY KEY(id))")

        session.execute("CREATE TABLE IF NOT EXISTS InputData " +
            "(id text, measurement text, metaDataID text, PRIMARY KEY(id))")

        session.execute("CREATE TABLE IF NOT EXISTS DataPoint " +
            "(id text, inputDataID text, value double, timestamp timestamp, PRIMARY KEY(id))")
    }


    /**
      * Appends data to a dataset of give id
      * @param inputData Data to insert into the database
      * @return Either Right(uuid), new id of inserted data, or Left(errormsg) on error
      */
    override def insertData(inputData: InputData): Either[String, String] = {

        val activityTypeID = UUID.randomUUID().toString
        val timeframeID = UUID.randomUUID().toString
        val metaDataID = UUID.randomUUID().toString

        val metaData = inputData.metadata
        val activityType = metaData.activityType
        val timeframe = metaData.timeframe

        session.execute("INSERT INTO ActivityType " +
            "(id, name, description) " +
            s"VALUES ('$activityTypeID', '${activityType.name}', '${activityType.description}')")


        session.execute("INSERT INTO Timeframe " +
            "(id, startTime, endTime) " +
            s"VALUES ('$timeframeID', '${timeframe.start}', '${timeframe.end}')")


        session.execute("INSERT INTO InputMetaData " +
            "(id, timeframeID, scaling, resolution, reliability, activityTypeID) " +
            s"VALUES ('$metaDataID', '$timeframeID', '${metaData.scaling.toString}', " +
            s"'${metaData.resolution.toString}', '${metaData.reliability.toString}', '$activityTypeID')")


        session.execute("INSERT INTO InputData " +
            "(id, measurement, metaDataID) " +
            s"VALUES ('${inputData.id}', '${inputData.measurement}', '$metaDataID')")

        inputData.dataPoints foreach (dataPoint => insertDataPoint(dataPoint, inputData.id))

        Right(inputData.id)
    }


    def insertDataPoint(dataPoint: DataPoint, inputDataUUId: String) = {
        val uuid = UUID.randomUUID().toString

        session.execute("INSERT INTO DataPoint " +
            "(id, inputDataID, value, timestamp) " +
            s"VALUES ('$uuid', '$inputDataUUId', ${dataPoint.value.value}, ${dataPoint.timstamp})")
    }




    def containsDataPointWithId(id: String): Boolean = {
        val resultSet = session.execute(s"SELECT * FROM InputData WHERE id = '${id}'")
        Option(resultSet.one()) match {
            case Some(r) => true
            case None => false
        }
    }

///////tested ^^^^ ////// untested vvvvv ///////////////



    /**
      * Retrieves a data set from the storage
      * @param id Id of the data to retrieve
      * @return Either the data set (Left) or Left(errormsg) on error
      */
    override def getDataSet(id: String): Either[String, InputData] = {

        val measurement = session.execute(s"SELECT measurement FROM InputData WHERE id = '${id}'").toString()

        val metaDataId = session.execute(s"SELECT MetaDataID FROM InputData Where id = '${id}'").toString
        val metadata = getInputMetaData(metaDataId)

        val dataPoints = getDataPointForInputData(id)

        var output =  new InputData(id, measurement, dataPoints, metadata)
        Right(output)

    }

  /**
    *
    * @param InputMetaDataId
    * @return metadata to given metaDataID used in getDataSet
    */
    def getInputMetaData(InputMetaDataId : String) : Either[String,InputMetadata] = {

      val timeframeID = session.execute((s"SELECT timeframeID FROM InputMetaData Where id = '${InputMetaDataId}'"))
        .one().getString("timeframeID").toString
      val tmstmp_start = session.execute((s"SELECT startTime FROM Timeframe Where id = '${timeframeID}'"))
      val tmstmp_end = session.execute((s"SELECT endTime FROM Timeframe Where id = '${timeframeID}'"))
      var timeframe = null
      //val timeframe = new Timeframe(tmstmp_start.one().getTime("startTime"),tmstmp_end.one().getTimestamp("endTime"))
    /**Problem: how to get data of type Timestamp from the resultset? => getTimestamp(attribute) deliveres Data, not Timestamp*/
        var activity = null
    //activity analog zu timeframe

      var rel = null
      val reliabilityString = session.execute(s"SELECT reliability FROM InputMetaData Where id = '${InputMetaDataId}'")
      .one().getString("reliability")
      /* switch case o.ä. on reliabilityString. "Device" => rel = reliability.Device etc */
      ///**wie geht das mit dem zugriff auf die Aufzählungstypen?

      // resolution & scaling analog zu reliability
      var res = null
      var scal = null

      var output = new InputMetadata(timeframe,rel,res,scal,activity)
      Right(output)
}

/**
*
* @param id id of the input data
* @return list of datapoints belonging to the given InputData
*/
def getDataPointForInputData(id : String) : Either[String, List[DataPoint]] = {

//selecting datapoints which have the InputData Id as "Foreign Key". Make List, return
???
}


////for meeting: do we really need the two methods below?/////////////////

/**
* Appends data to a data set of give id
* @param data Data to insert into the database
* @param id Id of existing data set to append to
* @return Either Right(id), id of appended data, or Left(errormsg) on error
*/
override def appendData(data: InputData, id: Int): Either[String, Int] = ???

/**
* Attempts to append data to a data set of give id.
* If the id does not exist, a new data set is created.
* @param data Data to insert into the database
* @param id Id of data set to append to
* @return Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
*/
override def appendOrInsertData(data: InputData, id: Int): Either[String, Int] = ???
}
