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
