package threesixty.visualizer.util.param


object PositionType extends Enumeration {
    case class Position(val name: String)
    val TOP = new Position("top")
    val BOTTOM = new Position("bottom")
    val LEFT = new Position("left")
    val RIGHT = new Position("right")

    def getPosition(name: String): Option[PositionType.Position] = {
        val result = name.toLowerCase match {
            case PositionType.TOP.name => Some(PositionType.TOP)
            case PositionType.BOTTOM.name => Some(PositionType.BOTTOM)
            case PositionType.LEFT.name => Some(PositionType.LEFT)
            case PositionType.RIGHT.name => Some(PositionType.RIGHT)
            case _ => None
        }

        result
    }
}

