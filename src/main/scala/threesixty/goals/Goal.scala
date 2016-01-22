package threesixty.goals


sealed trait Goal {

    val metadata:GoalMetadata

    /**
     * Calculated the degree of fulfillment of the goal
     * @return Value between 0.0 and 1.0
     */
    def degreeOfFulfillment:Double

    /**
     *  Returns whether the goal has been reached.
     *  @return true if goal is reached, false if not
     */
    def isReached:Boolean = degreeOfFulfillment == 1.0

}

trait SingleGoal extends Goal

trait MultiGoal extends Goal
