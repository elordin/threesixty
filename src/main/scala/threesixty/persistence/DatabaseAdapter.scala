package threesixty.persistence

import threesixty.data.InputData


trait DatabaseAdapter {

    /**
     *  Retrieves a data set from the storage
     *  @param id Id of the data to retrieve
     *  @returns Either the data set (Left) or Left(errormsg) on error
     */
    def getDataSet(id:Int):Either[String, InputData]

    /**
     *  Appends data to a dataset of give id
     *  @param data Data to insert into the database
     *  @returns Either Right(id), new id of inserted data, or Left(errormsg) on error
     */
    def insertData(data:InputData):Either[String, Int]

    /**
     *  Appends data to a data set of give id
     *  @param data Data to insert into the database
     *  @param id Id of existing data set to append to
     *  @returns Either Right(id), id of appended data, or Left(errormsg) on error
     */
    def appendData(data:InputData, id:Int):Either[String, Int]

    /**
     *  Attempts to append data to a data set of give id.
     *  If the id does not exist, a new data set is created.
     *  @param data Data to insert into the database
     *  @param id Id of data set to append to
     *  @returns Either Right(id), new id of inserted data or dataset appended to, or Left(errormsg) on error
     */
    def appendOrInsertData(data:InputData, id:Int):Either[String, Int]

}
