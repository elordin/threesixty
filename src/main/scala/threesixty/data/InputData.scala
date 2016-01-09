package threesixty.data

import threesixty.data.metadata.InputMetadata
import Data.{ValueType, Identifier}


case class DataPoint(val timstamp:Int, val value: ValueType)

case class InputData(
        val id: Identifier,
        val data: List[DataPoint],
        val metadata: InputMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
