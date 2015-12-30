package threesixty.metadata

import java.sql.Timestamp

/**
  * Created by Thomas on 30.12.2015.
  */
case class Timeframe(startc: Timestamp, endc: Timestamp) {
    var start: Timestamp = startc
    var end: Timestamp = endc

    require(start != null && end != null && start.before(end), "Start must be before the end.")
}
