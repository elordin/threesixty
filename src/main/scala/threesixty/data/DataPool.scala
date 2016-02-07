package threesixty.data

import threesixty.data.Data.{Identifier, Timestamp, DoubleValue}

import threesixty.persistence.DatabaseAdapter

import scala.collection.immutable.{Map => ImmutableMap}


/**
 *  This contains all datasets requested by the client.
 *
 *  @param dataIDs Set of IDs of datasets that will be processed.
 *  @param databaseAdapter DatabaseAdapter
 */
@throws[NoSuchElementException]("if an id was given, for which no InputData exists.")
class DataPool(
    val skeletons: Seq[InputDataSkeleton],
    implicit val databaseAdapter: DatabaseAdapter
) {

    require(skeletons.size > 0, "Empty Set if Input Data is not allowed.")

    val SIZE_THRESHOLD = 10000

    /** Reduces the amount of datapoints. */
    def prune(input: InputDataLike): InputDataLike = {
        if (input.dataPoints.size > SIZE_THRESHOLD) {
            val groupSize = math.ceil(input.dataPoints.size / SIZE_THRESHOLD).toInt
            val prunedDatapoints = input.dataPoints.grouped(groupSize).map({
                pts => val (sumTime, sumValue) = pts.tail.foldLeft(
                        (pts.head.timestamp.getTime, pts.head.value.value)
                    )({
                        (init, current) => (
                            init._1 + current.timestamp.getTime,
                            init._2 + current.value.value)
                    })
                    DataPoint(new Timestamp(sumTime / groupSize), DoubleValue(sumValue / groupSize))
            }).toList
            InputDataSubset(
                input.id,
                input.measurement,
                prunedDatapoints,
                input.metadata,
                input.metadata.timeframe.start,
                input.metadata.timeframe.end
            )
        } else {
            input
        }
    }


    // get data from db
    val inputDatasets: Seq[InputDataLike] =
        skeletons.distinct.map {
            case subsetSkeleton: InputDataSubsetSkeleton =>
                databaseAdapter.getDatasetInRange(subsetSkeleton.id, subsetSkeleton.from, subsetSkeleton.to) match {
                    case Right(subset: InputDataSubset) => prune(subset)
                    case Left(error: String)   => throw new NoSuchElementException(error)
                }
            case fullsetSkeleton: InputDataSkeleton =>
                databaseAdapter.getDataset(fullsetSkeleton.id) match {
                    case Right(fullset: InputData) => prune(fullset)
                    case Left(error: String)    => throw new NoSuchElementException(error)
                }
        }

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
    def getDatasets(ids: Identifier*): Seq[ProcessedData] =
        ids.map(processedDatasets(_))

    @throws[NoSuchElementException]("if a dataset was requested that is not in processedDatasets")
    def getDataset(id: Identifier): ProcessedData =
        processedDatasets(id)

    def apply(id: Identifier): Option[ProcessedData] = this.processedDatasets.get(id)

}

