package threesixty.data

import threesixty.data.metadata.{CompleteInputMetadata, IncompleteInputMetadata}
import Data.{Timestamp, ValueType, Identifier}


case class DataPoint(val timestamp:Timestamp, val value:ValueType)


case class UnsafeInputData(
	val identifier: Identifier,
	val measurement: String, //heartrate, temperature etc
	val dataPoints: List[DataPoint],
	val metadata: IncompleteInputMetadata
)


case class InputData(
	val identifier: Identifier,
	val measurement: String, //heartrate, temperature etc
	val dataPoints: List[DataPoint],
	val metadata: CompleteInputMetadata
) {

    require(dataPoints.length > 0, "Emtpy dataset not allowed.")

}
