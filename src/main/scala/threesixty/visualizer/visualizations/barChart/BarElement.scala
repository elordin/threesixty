package threesixty.visualizer.visualizations.barChart

import threesixty.visualizer.visualizations.general.RGBColor

import scala.xml.Elem

/**
  * @author Thomas Engel
  */
case class BarElement(val id: String,
                      val xLeft: Double,
                      val width: Double,
                      val height: Double,
                      val description: String,
                      val showValues: Boolean = false,
                      val value: String = "",
                      val fontSize: Option[Int] = None,
                      val color: Option[RGBColor] = None) {

    private def getColor: String = {
        if(color.isDefined) color.get.convertToColorString else ""
    }

    private def getFontSize: String = {
        if(fontSize.isDefined) fontSize.get.toString else ""
    }

    def getSVGElement: Elem = {
        val (dpx, dpy) = calculateDescriptionAnchorPoint
        val (vpx, vpy) = calculateValueAnchorPoint

        <g id={id}>
            <path class="bar"
                  fill={getColor}
                  d={calculateBarPath} />
            <text class="description"
                  x={dpx.toString}
                  y={dpy.toString}
                  font-family="Roboto, Segoe UI"
                  font-weight="100"
                  font-size={getFontSize}
                  text-anchor="middle">{description}</text>
            {if (showValues)
                <text class="value"
                      x={vpx.toString}
                      y={vpy.toString}
                      font-family="Roboto, Segoe UI"
                      font-weight="100"
                      font-size={getFontSize}
                      text-anchor="middle">{value}</text>
            }
        </g>
    }

    private def calculateBarPath: String = {
        val p1 = (xLeft, 0)
        val p2 = (xLeft, height)
        val p3 = (xLeft + width, height)
        val p4 = (xLeft + width, 0)

        "M " + p1._1 + " " + p1._2 +
        " L " + p2._1 + " " + p2._2 +
        " L " + p3._1 + " " + p3._2 +
        " L " + p4._1 + " " + p4._2 +
        " L " + p1._1 + " " + p1._2
    }

    private def calculateValueAnchorPoint: (Double, Double) = {
        val barMiddle = (xLeft + width / 2.0, height)
        val offset = if(height < 0) -10 else 5 + fontSize.getOrElse(15)

        (barMiddle._1, barMiddle._2 + offset)
    }

    private def calculateDescriptionAnchorPoint: (Double, Double) = {
        val baseMiddle = (xLeft + width / 2.0, 0)
        val offset = if(height < 0) 5 + fontSize.getOrElse(15) else - 10

        (baseMiddle._1, baseMiddle._2 + offset)
    }
}
