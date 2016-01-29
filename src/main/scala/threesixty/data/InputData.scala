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
	val dataPoints: Iterable[DataPoint],
	val metadata: IncompleteInputMetadata
) {
    require(dataPoints.size > 0, "Emtpy dataset not allowed.")
}


trait InputDataLike {
    def id: Identifier
    def measurement: String
    def dataPoints: Iterable[DataPoint]
    def metadata: CompleteInputMetadata
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
	val id: Identifier,
	val measurement: String, //heartrate, temperature etc
	val dataPoints: Iterable[DataPoint],
	val metadata: CompleteInputMetadata
) extends InputDataLike {
    require(dataPoints.size > 0, "Emtpy dataset not allowed.")
}

/*

case class LazyInputData(
    val id: Identifier,
    val measurement: String,
    val metadata: CompleteInputMetadata,
    implicit val dbAdapter: DatabaseAdapter
) extends InputDataLike with Iterator[DataPoint] {

    def dataPoints = this

    var currentTimestamp: Timestamp = new Timestamp(0)

    var buffer: Seq[DataPoint] = Seq()

    var bufferIterator = buffer.iterator

    def refillBuffer(): Unit = {
        ???
    }

    def hasNext = if (bufferIterator.hasNext) {
        true
    } else {
        refillBuffer()
        bufferIterator = buffer.iterator
        bufferIterator.next
    }

    def next = bufferIterator.next
}

*/
