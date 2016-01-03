package threesixty.goals

import threesixty.data.InputData
import threesixty.metadata.Timeframe

trait GoalMetadata

/**
  *  @author Thomas Engel
  */
case class IncompleteGoalMetadata(val timeframe: Option[Timeframe] = None)
    extends GoalMetadata {

    def complete(contextData: List[InputData]): CompleteGoalMetadata = {
        CompleteGoalMetadata(timeframe.getOrElse(Timeframe.deduce(contextData)))
    }
}

/**
  *  @author Thomas Engel
  */
case class CompleteGoalMetadata(val timeframe: Timeframe) extends GoalMetadata {
    require(timeframe != null, "Null value for timeframe not allowed")
}
