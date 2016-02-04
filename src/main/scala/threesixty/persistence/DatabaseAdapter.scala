package threesixty.persistence

import threesixty.data.InputData
import threesixty.data.Data.Identifier
import threesixty.data.metadata.CompleteInputMetadata

trait DatabaseAdapter {

    // def getDatasetInRange(id:Identifier, from: Timestamp, to: Timestamp)

    /**
     *  Retrieves a data set from the storage
     *  @param id Id of the data to retrieve
     *  @return Either the data set (Left) or Left(errormsg) on error
     */
    def getDataset(id:Identifier):Either[String, InputData]

    /**
     *  Appends data to a dataset of give id
     *  @param data Data to insert into the database
     *  @return Either Right(uuid), new id of inserted data, or Left(errormsg) on error
     */
    def insertData(data:InputData):Either[String, Identifier]

    /**
     *  Appends data to a data set of give id
     *  @param data Data to insert into the database
     *  @param id Id of existing data set to append to
     *  @return Either Right(id), id of appended data, or Left(errormsg) on error
     */
    def appendData(data:InputData):Either[String, Identifier]

    /**
     *  Attempts to append data to a data set of give id.
     *  If the id does not exist, a new data set is created.
     *  @param data Data to insert into the database
     *  @return Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
     */
    def appendOrInsertData(data:InputData):Either[String, Identifier]

    def getMetadata(id: Identifier): Option[CompleteInputMetadata]

}
