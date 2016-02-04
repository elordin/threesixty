package threesixty.persistence

import threesixty.data.InputData
import threesixty.data.Data.Identifier
import threesixty.data.metadata.CompleteInputMetadata


trait DatabaseAdapter {

    // def getDatasetInRange(id:Identifier, from: Timestamp, to: Timestamp)

    /**
      * Retrieves a data set from the storage
      *
      * @param id Id of the data to retrieve
      * @return Either the data set (Left) or Left(errormsg) on error
      */
    def getDataset(id: Identifier): Either[String, InputData]

    /**
      * Inserts data set into the storage
      *
      * @param data Data to insert into the database
      * @return Either Right(uuid), new id of inserted data, or Left(errormsg) on error
      */
    def insertData(data: InputData): Either[String, Identifier]

    /**
      *  Gets only the metadata for a datset with given ID.
      *
      *  @param identifier ID of data whose metadata is requested
      *  @return Some[CompleteInputMetadata] of the requested dataset or None on error
      */
    // def getMetadata(identifier: Identifier) : Option[CompleteInputMetadata]


    /**
     *  Gets only the metadata for a datset with given ID.
     *
     *  @param identifier ID of data whose metadata is requested
     *  @return Some[CompleteInputMetadata] of the requested dataset or None on error
     */
    def getMetadata(identifier: Identifier) : Option[CompleteInputMetadata]

}
