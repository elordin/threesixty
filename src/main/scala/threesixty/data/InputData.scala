package threesixty.data

import threesixty.data.metadata.InputMetadata
import Data.{ValueType, Identifier}
import java.sql.Timestamp


case class DataPoint(val timestamp:Timestamp, val value:AnyVal)
>>>>>>> 3b0c95f7b2b5bc3f6a2c6a4575adee65ef6c35b7

case class DataPoint(val timstamp:Int, val value: ValueType)

case class InputData(
        val id: Identifier,
        val data: List[DataPoint],
        val metadata: InputMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
