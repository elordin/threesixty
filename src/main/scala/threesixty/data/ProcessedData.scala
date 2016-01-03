package threesixty.data

import threesixty.data.tags.Tag
import Data.ValueType

case class TaggedDataPoint(
    val timestamp: Int,
    val value: ValueType,
    val tags:Set[Tag]
) {

}


case class ProcessedData(val data:List[TaggedDataPoint]) {

    require(data.length > 0, "Empty dataset not allowed.")

}
