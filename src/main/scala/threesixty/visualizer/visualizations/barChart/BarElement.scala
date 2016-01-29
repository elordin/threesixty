package threesixty.visualizer.visualizations.barChart

import threesixty.visualizer.visualizations.general.RGBColor

import scala.xml.Elem


/**
  * This class is used to generate a svg element for a bar.
  *
  * @param id the id for the corresponding svg element
  * @param xLeft x-coordinate of the left side of the bar
  * @param width the width of the bar
  * @param height the height of the bar (can also be negative)
  * @param description the description displayed for the bar
  * @param showValues iff the value should be shown to
  * @param value the shown value
  * @param fontSize the font size of labels
  * @param color the color of the bar
  *
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

    /**
      * @return the string for the color or an empty string if no color was set
      */
    private def getColor: String = {
        if(color.isDefined) color.get.convertToColorString else ""
    }

    /**
      * @return the string for the font size or an empty string if no font size was set
      */
    private def getFontSize: String = {
        if(fontSize.isDefined) fontSize.get.toString else ""
    }

    /**
      * @return the svg element for the bar
      */
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

    /**
      * @return the path string (<path d=.. />) for the bar
      */
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

    /**
      * @return the anchor point for the value label
      */
    private def calculateValueAnchorPoint: (Double, Double) = {
        val barMiddle = (xLeft + width / 2.0, height)
        val offset = if(height < 0) -10 else 5 + fontSize.getOrElse(15)

        (barMiddle._1, barMiddle._2 + offset)
    }

    /**
      * @return the anchor point for the description label
      */
    private def calculateDescriptionAnchorPoint: (Double, Double) = {
        val baseMiddle = (xLeft + width / 2.0, 0)
        val offset = if(height < 0) 5 + fontSize.getOrElse(15) else - 10

        (baseMiddle._1, baseMiddle._2 + offset)
    }
}
