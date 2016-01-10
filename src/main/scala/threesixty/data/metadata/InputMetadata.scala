package threesixty.data.metadata

import threesixty.data.InputData

trait InputMetadata

/**
 *  @author Thomas Weber
 */
case class IncompleteInputMetadata(
    val timeframe: Option[Timeframe] = None,
    val reliability: Option[Reliability.Value] = None,
    val resolution: Option[Resolution.Value] = None,
    val scaling: Option[Scaling.Value] = None,
    val activityType: Option[ActivityType] = None
) extends InputMetadata {

    def complete(contextData: InputData): CompleteInputMetadata = {
        CompleteInputMetadata(
            timeframe.getOrElse(Timeframe.deduce(contextData)),
            reliability.getOrElse(Reliability.deduce(contextData)),
            resolution.getOrElse(Resolution.deduce(contextData)),
            scaling.getOrElse(Scaling.deduce(contextData)),
            activityType.getOrElse(ActivityType.deduce(contextData))
        )
    }

}

/**
 *  @author Thomas Engel
 */
case class CompleteInputMetadata(
    val timeframe: Timeframe,
    val reliability: Reliability.Value,
    val resolution: Resolution.Value,
    val scaling: Scaling.Value,
    val activityType: ActivityType
) extends InputMetadata {

    require(timeframe != null, "Null value for timeframe not allowed")
    require(reliability != null, "Null value for reliability not allowed")
    require(resolution != null, "Null value for resolution not allowed")
    require(scaling != null, "Null value for scaling not allowed")
    require(activityType != null, "Null value for activityType not allowed")

}
