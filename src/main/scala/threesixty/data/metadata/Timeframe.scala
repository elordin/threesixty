package threesixty.data.metadata

import threesixty.data.{DataPoint, InputData}
import threesixty.data.Data.Timestamp

object Timeframe {
    def deduce(contextData: InputData): Timeframe = {
        Timeframe(contextData.dataPoints.head.timestamp, contextData.dataPoints.last.timestamp)
    }

    def deduce(contextData: List[InputData]): Timeframe = {
        val frames = contextData.map(deduce)
        val min = frames.reduceLeft((l,r) => if(l.start.before(r.start)) l else r)
        val max = frames.reduceLeft((l,r) => if(l.end.before(r.end)) r else l)
        Timeframe(min.start, max.end)
    }
}

/**
  * @author Thomas Engel
  */
case class Timeframe(val start: Timestamp, val end: Timestamp) {
    require(start != null, "Null value for start not allowed")
    require(end != null, "Null value for end not allowed")
    require(start.before(end), "Start must be before the end.")
}
