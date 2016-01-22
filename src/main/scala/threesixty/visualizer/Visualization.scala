package threesixty.visualizer

import threesixty.data.ProcessedData
import scala.xml.Elem


/**
 *  Generic visualization.
 *
 *  If they require additional parameters, use
 *  [[threesixty.visualizer.VisualizationCofnig]] to store those parameters
 *  and as a factory.
 *
 *  Use [[threesixty.visualizer.VisualizationMixins]] to add them to a [[threesixty.visualizer.Visualizer]].
 */
abstract class Visualization(data: Set[ProcessedData]) {

    override def toString(): String = toSVG.toString

    /**
     *  Returns a SVG/XML tree.
     */
    def toSVG:Elem

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}
