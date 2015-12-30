package threesixty.goals

import threesixty.metadata.Timeframe

case class GoalMetadata(timeframec: Timeframe) {
    val timeframe: Timeframe = timeframec
}
