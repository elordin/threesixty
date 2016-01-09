package threesixty.persistence.cassandra

import java.util.UUID

import threesixty.data.{DataPoint, InputData}
import threesixty.persistence.DatabaseAdapter

/**
  * Created by Stefan Cimander on 09.01.16.
  */
object CassandraAdapter extends DatabaseAdapter {

    val uri = CassandraConnectionUri("cassandra://localhost:9042/test")
    val session = CassandraConnector.createSessionAndInitKeyspace(uri)


    def createTables() = {

        /*
        session.execute("DROP TABLE ActivityType")
        session.execute("DROP TABLE Timeframe")
        session.execute("DROP TABLE InputMetaData")
        session.execute("DROP TABLE DataPoint")
        session.execute("DROP TABLE InputData")
        */


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


        createTables()



        val inputDataID = inputData.id
        val activityTypeID = UUID.randomUUID().toString
        val timeframeID = UUID.randomUUID().toString
        val metaDataID = UUID.randomUUID().toString



        val metaData = inputData.metadata
        val activityType = metaData.activityType
        val timeframe = metaData.timeframe



        //ActivityType
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
            s"VALUES ('$inputDataID', '${inputData.measurement}', '$metaDataID')")


        var dataPoints = inputData.data

        // Insert each point into the DataPoint table
        dataPoints foreach (dataPoint => insertDataPoint(dataPoint, inputDataID))


        Right(inputDataID)
    }


    def insertDataPoint(dataPoint: DataPoint, inputDataUUId: String) = {

        val uuid = UUID.randomUUID().toString

        session.execute("INSERT INTO DataPoint " +
            "(id, inputDataID, value, timestamp) " +
            s"VALUES ('$uuid', '$inputDataUUId', ${dataPoint.value.value}, ${dataPoint.timstamp})")

    }
























    /**
      * Retrieves a data set from the storage
      * @param id Id of the data to retrieve
      * @return Either the data set (Left) or Left(errormsg) on error
      */
    override def getDataSet(id: Int): Either[String, InputData] = ???

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
