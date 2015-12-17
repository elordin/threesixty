package threesixty.persistence

trait DatabaseAdapter {

    /**
     *  Retrieves a dataset from the storage
     *  @param id Id of the data to retrieve
     *  @returns Either the dataset (Left) or Right(errormsg) on error
     */
    // def getDataset(id:Int):Either[InputData, String]

    /**
     *  Appends data to a dataset of give id
     *  @param data Data to insert into the database
     *  @returns Either Left(id), new id of inserted data, or Right(errormsg) on error
     */
    // def insertData(data:InputData):Either[Int, String]

    /**
     *  Appends data to a dataset of give id
     *  @param data Data to insert into the database
     *  @param id Id of existing dataset to append to
     *  @returns None if successful, Some(errormsg) on error
     */
    // def appendData(data:InputData, id:Int):Option[String]

    /**
     *  Attempts to append data to a dataset of give id.
     *  If the id does not exist, a new dataset is created.
     *  @param data Data to insert into the database
     *  @param id Id of dataset to append to
     *  @returns Either Left(id), new id of inserted data or dataset appended to, or Right(errormsg) on error
     */
    // def appendOrInsertData(data:InputData, id:Int):Either[Int, String]

}
