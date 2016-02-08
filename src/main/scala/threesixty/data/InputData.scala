package threesixty.data

import threesixty.data.metadata.{CompleteInputMetadata, IncompleteInputMetadata}
import Data.{Timestamp, ValueType, Identifier}
import threesixty.data.tags.InputOrigin

import scala.collection.Iterator

case class DataPoint(val timestamp:Timestamp, val value:ValueType)


object UnsafeInputData {
	implicit def toInputData(unsafe: UnsafeInputData): InputData =
		InputData(
			id = unsafe.id,
			measurement = unsafe.measurement,
			dataPoints = unsafe.dataPoints,
			metadata = unsafe.metadata.complete(unsafe.dataPoints.toList)
		)
}

case class UnsafeInputData(
	val id: Identifier,
	val measurement: String, //heartrate, temperature etc
	val dataPoints: List[DataPoint],
	val metadata: IncompleteInputMetadata
) {
    require(dataPoints.size > 0, "Emtpy dataset not allowed.")
}


sealed trait Subset {
    def from: Timestamp
    def to: Timestamp
}

sealed trait InputDataSkeletonLike {
    def id: Identifier
    def measurement: String
    def metadata: CompleteInputMetadata
}

class InputDataSkeleton(
    val id: Identifier,
    val measurement: String,
    val metadata: CompleteInputMetadata
) extends InputDataSkeletonLike {
    def subset(from: Option[Timestamp], to: Option[Timestamp]): InputDataSubsetSkeleton =
        new InputDataSubsetSkeleton(id, measurement, metadata,
            from.getOrElse(metadata.timeframe.start),
            to.getOrElse(metadata.timeframe.end))

    def fill(dataPoints: List[DataPoint]): InputData =
        InputData(id, measurement, dataPoints, metadata)
}
/*
object InputDataSkeleton {
    implicit def fill(skeleton: InputDataSkeleton): InputData = ???
}
*/

class InputDataSubsetSkeleton(
    override val id: Identifier,
    override val measurement: String,
    override val metadata: CompleteInputMetadata,
    val from: Timestamp,
    val to: Timestamp
) extends InputDataSkeleton(id, measurement, metadata) with Subset {
}

/*
object InputDataSubsetSkeleton {
    implicit def fill(skeleton: InputDataSubsetSkeleton): InputDataSubset = ???
}
*/


sealed trait InputDataLike extends InputDataSkeletonLike {
    def dataPoints: List[DataPoint]
}

object InputDataLike {
    implicit def toProcessedData: (InputDataLike) => ProcessedData = {
    	input: InputDataLike =>
        	ProcessedData(input.id, input.dataPoints.map({
	            case DataPoint(timestamp, value) =>
	                TaggedDataPoint(timestamp, value, Set(InputOrigin(input)))
	            }).toList
            )
    }
}


case class InputData(
    override val id: Identifier,
    override val measurement: String, //heartrate, temperature etc
    val dataPoints: List[DataPoint],
    override val metadata: CompleteInputMetadata
) extends InputDataSkeleton(id, measurement, metadata) with InputDataLike {
    require(dataPoints.size > 0, "Emtpy dataset not allowed.")
    require(dataPoints.size == metadata.size, "Metadata incompatible with data points.")

    implicit def ordered: Ordering[Timestamp] = new Ordering[Timestamp] {
        def compare(x: Timestamp, y: Timestamp): Int = x compareTo y
    }

    def addNewDataPoints(newDataPoints: List[DataPoint]): InputData = {
        val pointsToAdd = newDataPoints.diff(this.dataPoints)
        val newSize = this.dataPoints.length + pointsToAdd.length
        val newStart = (this.dataPoints ++ pointsToAdd).minBy(_.timestamp).timestamp
        val newEnd = (this.dataPoints ++ pointsToAdd).maxBy(_.timestamp).timestamp

        this.copy(dataPoints = (this.dataPoints ++ pointsToAdd).sortBy(_.timestamp),
            metadata = this.metadata.copy(size = newSize,
                timeframe = this.metadata.timeframe.copy(start = newStart, end = newEnd)))
    }

    override def subset(from: Option[Timestamp], to: Option[Timestamp]): InputDataSubset =
        InputDataSubset(id, measurement, dataPoints.filter {
            case DataPoint(t, _) =>
                (!from.isDefined || t.getTime >= from.get.getTime) &&
                (!to.isDefined   || t.getTime <= to.get.getTime)
        }, metadata, from.getOrElse(metadata.timeframe.start), to.getOrElse(metadata.timeframe.end))
}


case class InputDataSubset(
    override val id: Identifier,
    override val measurement: String,
    val dataPoints: List[DataPoint],
    override val metadata: CompleteInputMetadata,
    override val from: Timestamp,
    override val to: Timestamp
) extends InputDataSubsetSkeleton(id, measurement, metadata, from, to) with InputDataLike {
    require(dataPoints.size > 0, "Emtpy dataset not allowed.")
}

