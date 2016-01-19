package threesixty.data

import com.websudos.phantom.dsl.UUID
import threesixty.data.metadata.{CompleteInputMetadata, IncompleteInputMetadata}
import Data.{Timestamp, ValueType, Identifier}

/*
case class DataPoint(val timstamp: Timestamp, val value: ValueType)

case class InputData(
        val id: String,
        val dataPoints: List[DataPoint],
=======
*/


case class DataPoint(val timestamp:Timestamp, val value:ValueType)


case class UnsafeInputData(
    val id: Identifier,
    val measurement: String, //heartrate, temperature etc
    val data: List[DataPoint],
    val metadata: IncompleteInputMetadata
)


case class InputData(
    val id: Identifier,
    val measurement: String, //heartrate, temperature etc
    val data: List[DataPoint],
    val metadata: CompleteInputMetadata
) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
