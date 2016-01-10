package threesixty.data

import threesixty.data.metadata.InputMetadata
import Data.{ValueType, Identifier}
import java.sql.Timestamp


case class DataPoint(val timestamp:Timestamp, val value:ValueType)


case class InputData(
        val id: Identifier,
        val data: List[DataPoint],
        val metadata: InputMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
