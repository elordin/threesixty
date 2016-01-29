package threesixty.data.metadata

import threesixty.data.{TaggedDataPoint, ProcessedData, DataPoint, InputData}
import threesixty.data.Data.Timestamp

import scala.reflect.internal.util.Collections


object Timeframe {

    // TODO changed from InputData -> List[DataPoint]. Might have impact on other deductions.
    def deduceInputData(contextData: List[DataPoint]): Timeframe = {
        deduce(contextData.map((x: DataPoint) => x.timestamp))
    }

    private def deduce(data: Iterable[Timestamp]) = {
        Timeframe(data.head, data.last)
    }

    def deduceInputData(contextData: Iterable[InputData]): Timeframe = {
        deduceMulti(contextData.map((d: InputData) => d.dataPoints.map((x: DataPoint) => x.timestamp)))
    }

    def deduceProcessedData(contextData: Iterable[ProcessedData]): Timeframe = {
        deduceMulti(contextData.map((d: ProcessedData) => d.dataPoints.map((x: TaggedDataPoint) => x.timestamp)))
    }

    private def deduceMulti(data: Iterable[Iterable[Timestamp]]): Timeframe = {
        val frames = data.map(deduce)
        val min = frames.reduceLeft((l,r) => if(l.start.before(r.start)) l else r)
        val max = frames.reduceLeft((l,r) => if(l.end.before(r.end)) r else l)
        Timeframe(min.start, max.end)
    }
}


/**
 *  @author Thomas Engel
 */
case class Timeframe(val start: Timestamp, val end: Timestamp) {
    require(start != null, "Null value for start not allowed")
    require(end != null, "Null value for end not allowed")
    require(start.before(end), "Start must be before the end.")

    def length: Long = end.getTime - start.getTime
}
