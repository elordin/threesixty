package threesixty.visualizer.visualizations.general

/**
  * @author Thomas Engel
  */
abstract class ColorScheme {
    var strokeIndex = -1
    var strokeMap: Map[String, String] = Map.empty

    def getColor(name: String): String = {
        if(strokeMap.contains(name)) {
            strokeMap.get(name).get
        } else {
            val strokes = getAvailableColors
            strokeIndex = (strokeIndex + 1) % strokes.size
            val color = strokes(strokeIndex)
            strokeMap += name -> color
            color
        }
    }

    def getAvailableColors: List[String]
}


case class DefaultColorScheme() extends ColorScheme {
    def getAvailableColors: List[String] = {
        List("#222222", "#444444", "#666666", "#888888", "#AAAAAA", "#CCCCCC")
    }
}

/*

object RGBColor {
    @throws[NumberFormatException]
    def apply(hex: String):RGBColor = apply(Integer.parseInt(hex, 16))
    def apply(hex: Int): RGBColor = RGBColor(hex % 0xFF, hex >> 8 & 0xFF, hex >> 16 & 0xFF)
}

case class RGBColor(red: Int, green: Int, blue: Int) {
    require(0 <= red && red < 256)
    require(0 <= green && green < 256)
    require(0 <= blue && blue < 256)
    override def toString(): String = s"rgb($red, $green, $blue)"
}

/**
 *  __INFINITE__ (!) cyclic Iterator
 *
 *  @example {{{
 *      ColorScheme.next // returns another color
 *  }}}
 */
object ColorScheme extends Iterator[RGBColor] {
    val colors = Seq(RGBColor("F44336"),
        RGBColor("E91E63"),
        RGBColor("9C27B0"),
        RGBColor("673AB7"),
        RGBColor("3F51B5"),
        RGBColor("2196F3"),
        RGBColor("03A9F4"),
        RGBColor("00BCD4"),
        RGBColor("009688"),
        RGBColor("4CAF50"),
        RGBColor("8BC34A"),
        RGBColor("CDDC39"),
        RGBColor("FFEB3B"),
        RGBColor("FFC107"),
        RGBColor("FF9800"),
        RGBColor("FF5722")
    )
    var colorIterator = colors.iterator

    def next = {
        if (!colorIterator.hasNext) {
            colorIterator = colors.iterator
        }
        colorIterator.next
    }
    def hasNext = true
}

*/
