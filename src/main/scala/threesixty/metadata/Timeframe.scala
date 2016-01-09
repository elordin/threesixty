package threesixty.metadata

import java.sql.Timestamp

/**
  * Created by Thomas on 30.12.2015.
  */
case class Timeframe(val start: Timestamp, val end: Timestamp) {
    require(start != null, "Null value for start not allowed")
    require(end != null, "Null value for end not allowed")
    require(start.before(end), "Start must be before the end.")
}
