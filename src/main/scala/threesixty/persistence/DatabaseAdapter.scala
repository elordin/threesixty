package threesixty.persistence

import threesixty.data.{InputData, InputDataSubset, InputDataSkeleton}
import threesixty.data.Data.{Timestamp, Identifier}
import threesixty.data.metadata.CompleteInputMetadata


trait DatabaseAdapter {

    // def getDatasetInRange(id:Identifier, from: Timestamp, to: Timestamp)

    /**
      * Retrieves a data set from the storage
      *
      * @param id Id of the data to retrieve
      * @return Either the data set (Right) or Left(errormsg) on error
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
    def getSkeleton(identifier: Identifier) : Either[String, InputDataSkeleton]

    /**
      * Retrieves a data set for a specific time range from the storage
      *
      * @param identifier Identifier of data to retreive
      * @param from       The start timestamp of the range
      * @param to         The end timestamp of the range
      * @return           Either the data set (Left) or an error message (Right)
      */
    def getDatasetInRange(identifier: Identifier, from: Timestamp, to: Timestamp): Either[String, InputDataSubset]


}
