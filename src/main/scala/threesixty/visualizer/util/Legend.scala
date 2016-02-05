package threesixty.visualizer.util

import threesixty.visualizer.Renderable

import scala.xml.Elem

object LegendPositionType extends Enumeration {
    case class LegendPosition(val name: String)
    val TOP = new LegendPosition("top")
    val BOTTOM = new LegendPosition("bottom")
    val LEFT = new LegendPosition("left")
    val RIGHT = new LegendPosition("right")
}

/**
  *  @author Thomas Engel
  */
object Legend {
    implicit def toXML(legend: Legend): Elem = legend.toSVG

    def getLegendPosition(name: String): Option[LegendPositionType.LegendPosition] = {
        val result = name.toLowerCase match {
            case LegendPositionType.TOP.name => Some(LegendPositionType.TOP)
            case LegendPositionType.BOTTOM.name => Some(LegendPositionType.BOTTOM)
            case LegendPositionType.LEFT.name => Some(LegendPositionType.LEFT)
            case LegendPositionType.RIGHT.name => Some(LegendPositionType.RIGHT)
            case _ => None
        }

        result
    }
}

case class Legend(x: Int,
                  y: Int,
                  symbolWidth: Int,
                  labels: Seq[(String, Option[RGBColor])],
                  labelSize: Int = 12,
                  labelFontFamily: String = "Roboto, Segoe UI",
                  labelFontWeight: Int = 100
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

    /**
     * @param color the optional color
     * @return the color string
     */
    private def toColorString(color: Option[RGBColor]): String = {
        color.map(_.toString()).getOrElse("")
    }

    def toSVG: Elem = {
        <g class="legend">
            {for (i <- 0 until labels.size) yield
                <g class={"Legend" + labels(i)._1.replace(' ', '_')}>
                    <path d={calculateLegendRectangle(i)}
                          stroke={toColorString(labels(i)._2)}
                          stroke-width="0"
                          fill={toColorString(labels(i)._2)}/>
                    <text x={(x + 2*symbolWidth).toString}
                          y={(y + (2 * i + 1) * symbolWidth).toString}
                          font-family={labelFontFamily}
                          font-weight={labelFontWeight.toString}
                          font-size={labelSize.toString}
                          text-anchor="left">{labels(i)._1}</text>
                </g>
            }
        </g>
    }
}