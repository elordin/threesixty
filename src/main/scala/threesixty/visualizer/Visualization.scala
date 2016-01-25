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

    def getConfig: VisualizationConfig

    def getSVGElements: List[Elem]

    def getTitleElement: xml.Elem = {
        <text x={(getConfig.calculateViewBox._1 + getConfig._width / 2.0).toString}
              y={(getConfig.upperLimit - getConfig._distanceTitle).toString}
              font-family="Roboto, Segoe UI"
              font-weight="100"
              font-size={(getConfig._fontSizeTitle).toString}
              text-anchor="middle">{getConfig._title}</text>
    }

    /**
     *  Returns a SVG/XML tree.
     */
    def toSVG:Elem = {
        val (vbX, vbY, width, height) = getConfig.calculateViewBox

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
