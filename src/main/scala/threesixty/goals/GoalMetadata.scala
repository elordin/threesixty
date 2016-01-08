package threesixty.goals

import threesixty.metadata.Timeframe

case class GoalMetadata(val timeframe: Timeframe) {
    require(timeframe != null, "Null value for timeframe not allowed")
}
