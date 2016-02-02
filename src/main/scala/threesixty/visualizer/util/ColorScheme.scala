package threesixty.visualizer.util


object RGBColor {
    val BLACK = RGBColor(0, 0, 0)
    val WHITE = RGBColor(255, 255, 255)

    @throws[NumberFormatException]
    def apply(hex: String):RGBColor = apply(Integer.parseInt(hex, 16))
    def apply(hex: Int): RGBColor = RGBColor(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF)
}

case class RGBColor(red: Int, green: Int, blue: Int) {
    require(0 <= red && red < 256)
    require(0 <= green && green < 256)
    require(0 <= blue && blue < 256)
    override def toString(): String = s"rgb($red, $green, $blue)"
    def toHexString: String = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue)
}

/**
 *  __INFINITE__ (!) cyclic Iterator
 *
 *  @example {{{
 *      ColorScheme.next // returns another color
 *      ColorSchem(15) // returns a list of 15 colors
 *  }}}
 */
trait ColorScheme extends Iterator[RGBColor] {
    val colors =
        Seq(RGBColor("F44336"),
            RGBColor("E91E63"),
            RGBColor("9C27B0"),
            RGBColor("673AB7"),
            RGBColor("3F51FB5"),
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
    def reset() = { colorIterator = colors.iterator }
    def apply(n: Int): List[RGBColor] = colorIterator.take(n).toList
}


object DefaultColorScheme extends ColorScheme

// object BlueColorScheme      extends ColorScheme {
//     override val colors = Seq(...)
// }
// object RedColorScheme       extends ColorScheme {
//     override val colors = Seq(...)
// }
// object GreenColorScheme     extends ColorScheme {
//     override val colors = Seq(...)
// }
// object YellowColorScheme    extends ColorScheme {
//     override val colors = Seq(...)
// }
// object OrangeColorScheme    extends ColorScheme {
//     override val colors = Seq(...)
// }
// object PurpleColorScheme    extends ColorScheme {
//     override val colors = Seq(...)
// }
// object PinkColorScheme      extends ColorScheme {
//     override val colors = Seq(...)
// }
