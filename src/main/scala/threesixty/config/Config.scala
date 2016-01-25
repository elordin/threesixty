package threesixty.config

import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier

import threesixty.persistence.DatabaseAdapter

import scala.collection.immutable.{Map => ImmutableMap}


/**
 *  This contains all datasets requested by the client.
 *
 *  @param dataIDs Set of IDs of datasets that will be processed.
 *  @param databaseAdapter DatabaseAdapter
 */
@throws[NoSuchElementException]("if an id was given, for which no InputData exists.")
class Config(
    val dataIDs: Set[Identifier],
    implicit val databaseAdapter: DatabaseAdapter
) {

    require(dataIDs.size > 0, "Empty ID Set is not allowed.")

    // get data from db
    val inputDatasets: Set[InputData] =
        dataIDs.map(databaseAdapter.getDataset(_) match {
            case Right(data) => data
            case Left(error) => throw new NoSuchElementException(error)
        })

    // convert input data to processed data
    var processedDatasets: Map[Identifier, ProcessedData] =
        (for { data <- inputDatasets} yield (data.id, data: ProcessedData)).toMap

    /**
     *  Inserts data into the processedDatasets Map
     *  @param data Set of data to be added to processedDatasets
     */
    def pushData(data: Set[ProcessedData]): Unit = {
        data.foreach {
            d => processedDatasets += (d.id -> d)
        }
    }

    /**
     *  Accessor for processedDatasets
     *  @return Immutable Map of [[threesixty.data.Data.Identifier]] -> [[threesixty.data.ProcessedData]]
     */
    def datasets: ImmutableMap[Identifier, ProcessedData] = processedDatasets


    @throws[NoSuchElementException]("if a dataset was requested that is not in processedDatasets")
    def getDatasets(ids: Set[Identifier]): Set[ProcessedData] =
        ids.map(processedDatasets(_))

    @throws[NoSuchElementException]("if a dataset was requested that is not in processedDatasets")
    def getDataset(id: Identifier): ProcessedData =
        processedDatasets(id)

}

