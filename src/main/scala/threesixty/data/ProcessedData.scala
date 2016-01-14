package threesixty.data

import java.sql.Timestamp

import threesixty.data.tags.{Tag, InputOrigin}
import Data.ValueType

case class TaggedDataPoint(
                            val timestamp: Timestamp,
                            val value: ValueType,
                            val tags:Set[Tag]
) {

}


case class ProcessedData(val data:List[TaggedDataPoint]) {

    require(data.length > 0, "Empty dataset not allowed.")

}


object Implicits {

    implicit def inputToProcessedData:(InputData) => ProcessedData = {
        case input@InputData(_, _, data:List[DataPoint], metadata) =>
            ProcessedData(data.map {
                case DataPoint(timestamp, value) =>
                    TaggedDataPoint(timestamp, value, Set(InputOrigin(input)))
                })
    }

}
