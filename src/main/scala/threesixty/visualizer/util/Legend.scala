package threesixty.visualizer.util

import threesixty.visualizer.Renderable

import scala.xml.Elem


/**
  *  @author Thomas Engel
  */
object Legend {
    implicit def toXML(legend: Legend): Elem = legend.toSVG
}

case class Legend(x: Int,
                  y: Int,
                  symbolWidth: Int,
                  labels: Seq[(String, RGBColor)],
                  labelSize: Int,
                  labelFontFamily: String
) extends Renderable {

    /**
      * @param index the index of the legend entry
      * @return the path (<path d=.. />) for the legend symbol
      */
    private def calculateLegendRectangle(index: Int): String = {
        val yTop = y + index * 2 * symbolWidth

        "M " + x + " " + yTop +
            " L " + (x + symbolWidth) + " " + yTop +
            " L " + (x + symbolWidth) + " " + (yTop + symbolWidth) +
            " L " + x + " " + (yTop + symbolWidth)
    }


    def toSVG: Elem = {
        <g class="legend">
            {for (i <- 0 until labels.size) yield
                <g class={"Legend" + labels(i)._1.replace(' ', '_')}>
                    <path d={calculateLegendRectangle(i)}
                          stroke={ labels(i)._2.toString }
                          stroke-width="0"
                          fill={ labels(i)._2.toString }/>
                    <text x={(x + 2*symbolWidth).toString}
                          y={(y + (2 * i + 1) * symbolWidth).toString}
                          font-family={labelFontFamily}
                          font-size={labelSize.toString}
                          text-anchor="left">{labels(i)._1}</text>
                </g>
            }
        </g>
    }
}
