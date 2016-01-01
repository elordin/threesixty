package threesixty.metadata

/**
  * Created by Thomas on 30.12.2015.
  */
case class ActivityType(var name: String) {
    var description: String = null

    require(name != null && name.length() > 0, "Null value or empty string is not allowed")

    def setDescription(desc: String) {
        description = desc
    }
}
