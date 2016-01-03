package threesixty.data

import java.sql.Timestamp

import threesixty.data.metadata.CompleteInputMetadata

case class DataPoint(val timestamp:Timestamp, val value:AnyVal)


case class InputData(
        val id:AnyVal,
        val data: List[DataPoint],
        val metadata: CompleteInputMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
