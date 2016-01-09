package threesixty.persistence

import threesixty.data.InputData
import threesixty.data.Data.Identifier


trait DatabaseAdapter {

    /**
     *  Retrieves a dataset from the storage
     *  @param id Id of the data to retrieve
     *  @returns Either the dataset (Left) or Left(errormsg) on error
     */
    def getDataset(id:Identifier):Either[String, InputData]

    /**
     *  Appends data to a dataset of give id
     *  @param data Data to insert into the database
     *  @returns Either Right(id), new id of inserted data, or Left(errormsg) on error
     */
    def insertData(data:InputData):Either[String, Identifier]

    /**
     *  Appends data to a dataset of give id
     *  @param data Data to insert into the database
     *  @param id Id of existing dataset to append to
     *  @returns Either Right(id), id of appended data, or Left(errormsg) on error
     */
    def appendData(data:InputData, id:Identifier):Either[String, Identifier]

    /**
     *  Attempts to append data to a dataset of give id.
     *  If the id does not exist, a new dataset is created.
     *  @param data Data to insert into the database
     *  @param id Id of dataset to append to
     *  @returns Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
     */
    def appendOrInsertData(data:InputData, id:Identifier):Either[String, Identifier]

}
