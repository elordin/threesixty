package threesixty.data.metadata

import threesixty.metadata._

/**
  * Created by Thomas on 30.12.2015.
  */
case class InputMetadata(val timeframe: Timeframe,
                         val reliability: Reliability.type,
                         val resolution: Resolution.type,
                         val scaling: Scaling.type,
                         val activityType: ActivityType) {
    require(timeframe != null, "Null value for timeframe not allowed")
    require(reliability != null, "Null value for reliability not allowed")
    require(resolution != null, "Null value for resolution not allowed")
    require(scaling != null, "Null value for scaling not allowed")
    require(activityType != null, "Null value for activityType not allowed")
}
