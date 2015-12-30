package threesixty.metadata

/**
  * Created by Thomas on 30.12.2015.
  */
case class ActivityType(namec: String) {
    var name: String = namec
    var description: String = null

    require(name != null && name.length() > 0, "Name must be set.")

    def setDescription(desc: String) {
        description = desc
    }
}
