package threesixty.metadata

import java.sql.Timestamp

/**
  * Created by Thomas on 30.12.2015.
  */
case class Timeframe(val startTime: Timestamp, val endTime: Timestamp) {
    require(startTime != null, "Null value for start not allowed")
    require(endTime != null, "Null value for end not allowed")
    require(startTime.before(endTime), "Start must be before the end.")
}
