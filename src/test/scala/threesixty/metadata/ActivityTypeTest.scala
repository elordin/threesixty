package threesixty.data.metadata

import org.scalatest.FlatSpec

/**
  * @author Thomas Engel
  */
class ActivityTypeTestSpec extends FlatSpec {

    "An ActivityType" should "create a default ActivityType for input data" in {
        assertResult("Unknown") {ActivityType.deduce(null).name}
        assertResult("This ActivityType was automatically deduced") {ActivityType.deduce(null).description}
    }
}
