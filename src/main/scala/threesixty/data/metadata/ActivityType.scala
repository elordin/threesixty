package threesixty.data.metadata

import threesixty.data.InputData

object ActivityType {
    def deduce(contextData: InputData): ActivityType = {
        val a = new ActivityType("Unknown")
        a.setDescription("This ActivityType was automatically deduced")
        a
    }
}

/**
  * @author Thomas Engel
  */
case class ActivityType(var name: String) {
    var description: String = null

    require(name != null && name.length() > 0, "Null value or empty string is not allowed")

    def setDescription(desc: String) {
        description = desc
    }
}
