package threesixty.visualizer

import threesixty.data.ProcessedData
import scala.xml.Elem


/**
 *  Generic visualization.
 *
 *  If they require additional parameters, use
 *  [[threesixty.visualizer.VisualizationConfig]] to store those parameters
 *  and as a factory.
 *
 *  Use [[threesixty.visualizer.VisualizationMixins]] to add them to a [[threesixty.visualizer.Visualizer]].
 */
abstract class Visualization(data: Set[ProcessedData]) {

    override def toString(): String = toSVG.toString

    def config: VisualizationConfig

    def getSVGElements: List[Elem]

    def getTitleElement: xml.Elem = {
        <text x={(config.calculateViewBox._1 + config._width / 2.0).toString}
              y={(config.upperLimit - config._distanceTitle).toString}
              font-family="Roboto, Segoe UI, Sans-Serif"
              font-weight="100"
              font-size={(config._fontSizeTitle).toString}
              text-anchor="middle">{config._title}</text>
    }

    /**
     *  Returns a SVG/XML tree.
     */
    def toSVG:Elem = {
        val (vbX, vbY, width, height) = config.calculateViewBox

        <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox={vbX + " " + vbY + " " + width + " " + height} xml:space="preserve">
            {for(elem <- getSVGElements) yield
                elem
            }
            <g id="title">
                {getTitleElement}
            </g>
        </svg>
    }

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}
