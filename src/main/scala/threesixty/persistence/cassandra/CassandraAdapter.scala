package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.UUID

import com.datastax.driver.core.Row
import threesixty.data.metadata.InputMetadata
import threesixty.data.{DataPoint, InputData}
import threesixty.metadata.Reliability
import threesixty.metadata.Reliability._
import threesixty.metadata.Resolution
import threesixty.metadata.Resolution._
import threesixty.metadata.Scaling._
import threesixty.metadata._
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
            s"VALUES ('$timeframeID', '${timeframe.startTime}', '${timeframe.endTime}')")


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

        val measurement = session.execute(s"SELECT measurement FROM InputData WHERE id = '${id}'").one().getString("measurement")

        val metaDataId = session.execute(s"SELECT MetaDataID FROM InputData Where id = '${id}'").one().getString(("MetaDataID"))

        val metadata = getInputMetaData(metaDataId)

        val meta = metadata match {
            case Left(errormsg) => return Left(errormsg)
            case Right(x) => x
        }

        val dataPoints = getDataPointForInputData(id)
        val points = dataPoints match {
            case Left(errormsg) => return Left(errormsg)
            case Right(x) => x
        }

        val output =  new InputData(id, measurement, points, meta)
        Right(output)

    }

  /**
    *
    * @param InputMetaDataId
    * @return metadata to given metaDataID used in getDataSet
    */
    def getInputMetaData(InputMetaDataId : String) : Either[String,InputMetadata] = {

      //val timeframeID = "93857940-9c35-45f1-aa15-a08d9c5f4202"
 //   val InputMetaDataId = "bb80626b-f301-4945-bc35-40b37e4ff06a"
    val timeframeID_SET = session.execute("SELECT timeframeId FROM InputMetaData Where id = '" + InputMetaDataId + "'")
    val activityTypeID_SET = session.execute("SELECT activityTypeID FROM InputMetaData Where id = '" + InputMetaDataId + "'")

    //Sending ErrorMessage
    if (timeframeID_SET == null || activityTypeID_SET == null)
      return Left("Something went wrong while fetching metadata from the Database. ")

  //timeframe
    var timeframeID= timeframeID_SET.one().getString("timeframeID").toString()
    val start = session.execute("SELECT startTime FROM Timeframe Where id = '"+timeframeID+"'").one()
    val end = session.execute((s"SELECT endTime FROM Timeframe Where id = '"+timeframeID+"'")).one()
    val startTime = new Timestamp(start.getTimestamp("startTime").getTime())
    val endTime = new Timestamp(end.getTimestamp("endTime").getTime())
    val timeframe = new Timeframe(startTime, endTime)

  //activity
    val activityTypeID = activityTypeID_SET.one().getString("activityTypeID")
    val activity_name = session.execute("SELECT name FROM activityType Where id = '" + activityTypeID + "'" ).one().getString("name")
    val activity_desc = session.execute("SELECT description FROM activityType Where id = '" + activityTypeID + "'" ).one().getString("description")
     var activity = new ActivityType("name")
    activity.setDescription(activity_desc)




      val reliabilityName = session.execute(s"SELECT reliability FROM InputMetaData WHERE id = '${InputMetaDataId}'").one().getString("reliability")
      val reliability = Reliability.withName(reliabilityName)

      val resolutionName = session.execute(s"SELECT resolution FROM InputMetaData WHERE id = '${InputMetaDataId}'").one().getString("resolution")
      var resolution = Resolution.withName(resolutionName)

      val scalingName = session.execute(s"SELECT scaling FROM InputMetaData WHERE id = '${InputMetaDataId}'").one().getString("scaling")
      var scaling = Scaling.withName(scalingName)

      var output = new InputMetadata(timeframe, reliability, resolution, scaling, activity)
      Right(output)
}

/**
*
* @param id id of the input data
* @return list of datapoints belonging to the given InputData
*/
def getDataPointForInputData(id : String) : Either[String, List[DataPoint]] = {

  //fetching timestamp + values
  val resultSet_timestamp = session.execute("SELECT timestamp FROM datapoint Where InputdataId = '"+ id +"'" )
  val resultSet_value= session.execute(" SELECT value FROM datapoint Where InputdataId = '" + id + "'")

  //Sending ErrorMessage
  if (resultSet_timestamp == null || resultSet_value == null)
    return Left("Something went wrong while fetching metadata from the Database. ")

  val output = List()

  while ((!resultSet_timestamp.isExhausted) && (!resultSet_value.isExhausted))
    {
      var timestamp = new Timestamp(resultSet_timestamp.one().getTimestamp("timestamp").getTime())
      var value0 = resultSet_value.one().getDouble("value")
      var point = new DataPoint(timestamp,value = value0)
     output.++:(List(point))
    }

 Right( output)

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
