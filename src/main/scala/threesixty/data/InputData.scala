package threesixty.data

import threesixty.data.metadata.{CompleteInputMetadata, IncompleteInputMetadata}
import Data.{Timestamp, ValueType, Identifier}
import threesixty.data.tags.InputOrigin

case class DataPoint(val timestamp:Timestamp, val value:ValueType)


object UnsafeInputData {
	implicit def toInputData(unsafe: UnsafeInputData): InputData =
		InputData(
			id = unsafe.id,
			measurement = unsafe.measurement,
			dataPoints = unsafe.dataPoints,
			metadata = unsafe.metadata.complete(unsafe.dataPoints)
		)
}

case class UnsafeInputData(
	val id: Identifier,
	val measurement: String, //heartrate, temperature etc
	val dataPoints: List[DataPoint],
	val metadata: IncompleteInputMetadata
) {
    require(dataPoints.length > 0, "Emtpy dataset not allowed.")
}


object InputData {
    implicit def toProcessedData:(InputData) => ProcessedData = {
    	case input@InputData(id: Identifier, _, data:List[DataPoint], metadata) =>
        	ProcessedData(id, data.map {
	            case DataPoint(timestamp, value) =>
	                TaggedDataPoint(timestamp, value, Set(InputOrigin(input)))
	            }
            )
    }
}

case class InputData(
	val id: Identifier,
	val measurement: String, //heartrate, temperature etc
	val dataPoints: List[DataPoint],
	val metadata: CompleteInputMetadata
) {
    require(dataPoints.length > 0, "Emtpy dataset not allowed.")
}
