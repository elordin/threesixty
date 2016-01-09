package threesixty.data

import threesixty.data.tags.{Tag, InputOrigin}
import Data.{ValueType, Identifier}

case class TaggedDataPoint(
    val timestamp: Int,
    val value: ValueType,
    val tags:Set[Tag]
)


case class ProcessedData(val id: Identifier, val data:List[TaggedDataPoint]) {

    require(data.length > 0, "Empty dataset not allowed.")

}


object Implicits {

    implicit def input2ProcessedData:(InputData) => ProcessedData = {
        case input@InputData(id: Identifier, data:List[DataPoint], metadata) =>
            // error free
            // sanitized
            // metadata is complete
            ProcessedData(id, data.map {
                case DataPoint(timestamp, value) =>
                    TaggedDataPoint(timestamp, value, Set(InputOrigin(input)))
                })
    }

}
