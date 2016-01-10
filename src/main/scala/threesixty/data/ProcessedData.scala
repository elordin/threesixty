package threesixty.data

import threesixty.data.tags.{Tag}
import Data.{Timestamp, ValueType, Identifier}

case class TaggedDataPoint(
    val timestamp: Timestamp,
    val value: ValueType,
    val tags:Set[Tag]
)


case class ProcessedData(val id: Identifier, val data:List[TaggedDataPoint]) {

    require(data.length > 0, "Empty dataset not allowed.")

}
