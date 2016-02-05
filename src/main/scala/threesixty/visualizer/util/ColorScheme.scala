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

object ColorScheme {
    def getColorScheme(name: String): Option[ColorScheme] = {
        val colorScheme = name.toLowerCase match {
            case "none" => None
            case "blue" => Some(BlueColorScheme)
            case "red" => Some(RedColorScheme)
            case "green" => Some(GreenColorScheme)
            case "yellow" => Some(YellowColorScheme)
            case "orange" => Some(OrangeColorScheme)
            case "purple" => Some(PurpleColorScheme)
            case "pink" => Some(PinkColorScheme)
            case _ => Some(DefaultColorScheme)
        }

        colorScheme
    }
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
    def colors: Seq[RGBColor]

    lazy val infiniteColors: Stream[RGBColor] = Stream.continually(colors.toStream).flatten
    lazy val iterator = infiniteColors.iterator

    def next: RGBColor = iterator.next
    def hasNext: Boolean = true
    def apply(n: Int): List[RGBColor] = this.take(n).toList
}


object DefaultColorScheme extends ColorScheme {
    def colors = Seq(
            RGBColor("F44336"),
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
}

object BlueColorScheme    extends ColorScheme {
    def colors = Seq(
        RGBColor(0xE8EAF6),
        RGBColor(0xC5CAE9),
        RGBColor(0x9FA8DA),
        RGBColor(0x7986CB),
        RGBColor(0x5C6BC0),
        RGBColor(0x3F51B5),
        RGBColor(0x3949AB),
        RGBColor(0x303F9F),
        RGBColor(0x283593),
        RGBColor(0x1A237E)
    )
}

object RedColorScheme       extends ColorScheme {
    def colors = Seq(
        RGBColor(0xFFEBEE),
        RGBColor(0xFFCDD2),
        RGBColor(0xEF9A9A),
        RGBColor(0xE57373),
        RGBColor(0xEF5350),
        RGBColor(0xF44336),
        RGBColor(0xE53935),
        RGBColor(0xD32F2F),
        RGBColor(0xC62828),
        RGBColor(0xB71C1C)
    )
}

object GreenColorScheme     extends ColorScheme {
    def colors = Seq(
        RGBColor(0xE8F5E9),
        RGBColor(0xC8E6C9),
        RGBColor(0xA5D6A7),
        RGBColor(0x81C784),
        RGBColor(0x66BB6A),
        RGBColor(0x4CAF50),
        RGBColor(0x43A047),
        RGBColor(0x388E3C),
        RGBColor(0x2E7D32),
        RGBColor(0x1B5E20)
    )
}

object YellowColorScheme    extends ColorScheme {
    def colors = Seq(
        RGBColor(0xFFFDE7),
        RGBColor(0xFFF9C4),
        RGBColor(0xFFF59D),
        RGBColor(0xFFF176),
        RGBColor(0xFFEE58),
        RGBColor(0xFFEB3B),
        RGBColor(0xFDD835),
        RGBColor(0xFBC02D),
        RGBColor(0xF9A825),
        RGBColor(0xF57F17)
    )
}

object OrangeColorScheme    extends ColorScheme {
    def colors = Seq(
        RGBColor(0xFFF3E0),
        RGBColor(0xFFE0B2),
        RGBColor(0xFFCC80),
        RGBColor(0xFFB74D),
        RGBColor(0xFFA726),
        RGBColor(0xFF9800),
        RGBColor(0xFB8C00),
        RGBColor(0xF57C00),
        RGBColor(0xEF6C00),
        RGBColor(0xE65100)
    )
}

object PurpleColorScheme    extends ColorScheme {
    def colors = Seq(
        RGBColor(0xF3E5F5),
        RGBColor(0xE1BEE7),
        RGBColor(0xCE93D8),
        RGBColor(0xBA68C8),
        RGBColor(0xAB47BC),
        RGBColor(0x9C27B0),
        RGBColor(0x8E24AA),
        RGBColor(0x7B1FA2),
        RGBColor(0x6A1B9A),
        RGBColor(0x4A148C)
    )
}

object PinkColorScheme      extends ColorScheme {
    def colors = Seq(
        RGBColor(0xFCE4EC),
        RGBColor(0xF8BBD0),
        RGBColor(0xF48FB1),
        RGBColor(0xF06292),
        RGBColor(0xEC407A),
        RGBColor(0xE91E63),
        RGBColor(0xD81B60),
        RGBColor(0xC2185B),
        RGBColor(0xAD1457),
        RGBColor(0x880E4F)
    )
}
