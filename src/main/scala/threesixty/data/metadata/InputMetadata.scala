package threesixty.data.metadata

import threesixty.metadata._

/**
  * Created by Thomas on 30.12.2015.
  */
case class InputMetadata(timeframec: Timeframe,
                         reliabilityc: Reliability.type,
                         resolutionc: Resolution.type,
                         scalingc: Scaling.type,
                         activityTypec: ActivityType) {
    val timeframe: Timeframe = timeframec
    val reliability: Reliability.type = reliabilityc
    val resolution: Resolution.type = resolutionc
    val scaling: Scaling.type = scalingc
    val activityType: ActivityType = activityTypec
}