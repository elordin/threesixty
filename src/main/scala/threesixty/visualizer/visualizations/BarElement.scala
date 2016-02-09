package threesixty.visualizer.visualizations

import threesixty.visualizer.util.RGBColor

import scala.xml.Elem


/**
 * This class is used to generate a svg element for a bar.
 *
 * @param identifier the class for the corresponding svg element
 * @param xLeft x-coordinate of the left side of the bar
 * @param width the width of the bar
 * @param height the height of the bar (can also be negative)
 * @param description the description displayed for the bar
 * @param descriptionLabelSize the font size of the description labels
 * @param descriptionLabelFontFamily the font family of the description label
 * @param classes the classes for this svg element
 * @param showValues iff the value should be shown to
 * @param value the shown value
 * @param valueLabelSize the font size of value labels
 * @param valueLabelFontFamily the font family of the value label
 * @param color the color of the bar
 *
 * @author Thomas Engel
 */
case class BarElement(
    val identifier: String,
    val xLeft: Double,
    val width: Double,
    val height: Double,
    val description: String,
    val descriptionLabelSize: Int = 12,
    val descriptionLabelFontFamily: String = "Roboto, Segoe UI",
    val classes: Set[String],
    val showValues: Boolean = false,
    val value: String = "",
    val valueLabelSize: Int = 12,
    val valueLabelFontFamily: String =  "Roboto, Segoe UI",
    val color: RGBColor = threesixty.visualizer.util.RGBColor.TRANSPARENT
  ) {

    /**
     * @return the string for the color or an empty string if no color was set
     */
    private def getColor: String = color.toHexString

    /**
     * @return the svg element for the bar
     */
    def getSVGElement: Elem = {
        val (dpx, dpy) = calculateDescriptionAnchorPoint
        val (vpx, vpy) = calculateValueAnchorPoint

        <g class={identifier.replace(' ', '_') + " " + (classes.map(_.replace(' ', '_')) mkString " ") }>
            <path
                class="bar"
                fill={getColor}
                d={calculateBarPath} />
            <text
                class="description"
                x={dpx.toString}
                y={dpy.toString}
                font-size={descriptionLabelSize.toString}
                font-family={descriptionLabelFontFamily}
                text-anchor="middle">{description}</text>
            {if (showValues)
                <text
                    class="value"
                    x={vpx.toString}
                    y={vpy.toString}
                    font-size={valueLabelSize.toString}
                    font-family={valueLabelFontFamily}
                    text-anchor="middle">{value}</text>
            }
        </g>
    }

    /**
     * @return the path string (<path d=.. />) for the bar
     */
    def calculateBarPath: String = {
        val p1 = (xLeft, 0)
        val p2 = (xLeft, -height)
        val p3 = (xLeft + width, -height)
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
    def calculateValueAnchorPoint: (Double, Double) = {
        val barMiddle = (xLeft + width / 2.0, height)
        val offset = if(height < 0) -10 else 5 + valueLabelSize

        (barMiddle._1, barMiddle._2 + offset)
    }

    /**
     * @return the anchor point for the description label
     */
    def calculateDescriptionAnchorPoint: (Double, Double) = {
        val baseMiddle = (xLeft + width / 2.0, 0)
        val offset = if(height < 0) 5 + descriptionLabelSize else - 10

        (baseMiddle._1, baseMiddle._2 - offset)
    }
}
