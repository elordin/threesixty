package threesixty.visualizer.visualizations

import threesixty.visualizer.util.RGBColor

import scala.xml.Elem

/**
  * @author Thomas Engel
  */
object Segment {
    /**
      * @param angle the angle
      * @param radius the radius
      * @return the x-coordinate of the point on the circle
      */
    def calculateXCoordinate(angle: Double, radius: Double): Double = {
        radius * math.cos(math.toRadians(angle))
    }

    /**
      * @param angle the angle
      * @param radius the radius
      * @return the y-coordinate of the point on the circle
      */
    def calculateYCoordinate(angle: Double, radius: Double): Double = {
        - radius * math.sin(math.toRadians(angle))
    }

    /**
      * @param angle the angle
      * @param radius the radius
      * @return the point on the circle
      */
    def calculatePoint(angle: Double, radius: Double): (Double, Double) = {
        (calculateXCoordinate(angle, radius), calculateYCoordinate(angle, radius))
    }

    /**
      * @param query the query angle
      * @param start the start angle
      * @param end the end angle
      * @return true iff the query angle is withing the start and end angle
      */
    def isAngleContained(query: Double, start: Double, end: Double): Boolean = {
        (start <= query && query <= end) || (end <= query && query <= start)
    }
}


/**
  * This class is used to generate a svg element for a circle segment.
  *
  * @param id the id for the corresponding svg element
  * @param description the description displayed for the bar
  * @param angleStart the start angle
  * @param angleEnd the end angle
  * @param radius the radius
  * @param innerRadius the radius of the circle which is cutted out
  * @param valueRadius the radius of the circle where the value is shown
  * @param value the shown value
  * @param fontSize the font size of labels
  * @param color the color of the bar
  *
  * @author Thomas Engel
  */
case class Segment(
    val id: String,
    val description: String,
    val angleStart: Double,
    val angleEnd: Double,
    val radius: Double,
    val innerRadius: Double,
    val valueRadius: Double,
    val value: String,
    val fontSize: Option[Int] = None,
    val color: Option[RGBColor] = None
  ) {

    /**
     * @return the svg element representing the circle segment
     */
    def getSVGElement: Elem = {
        val (tlpx, tlpy) = calculateValueLabelAnchorPoint

        <g id={id}>
            <path class="segment"
                  fill={getColor}
                  d={calculatePath} />
            <path class="valuePath"
                  stroke="#000000"
                  d={calculateLabelPath}/>
            <text class="value"
                  x={tlpx.toString}
                  y={tlpy.toString}
                  font-family="Roboto, Segoe UI"
                  font-weight="100"
                  font-size={getFontSize}
                  text-anchor={calculateValueLabelAnchor}>{value}</text>
        </g>
    }

    /**
      * @return the string for the color or an empty string if no color was set
      */
    def getColor: String = {
        if(color.isDefined) color.get.toHexString else ""
    }

    /**
      * @return the string for the font size or an empty string if no font size was set
      */
    def getFontSize: String = {
        if(fontSize.isDefined) fontSize.get.toString else ""
    }

    /**
      * @return the angle difference
      */
    def calculateDeltaAngles: Double = {
        angleEnd - angleStart
    }

    /**
      * @return the path (<path d=.. />) for the segment
      */
    def calculatePath: String = {
        val largeArcFlag = getLargeArcFlag

        val p1 = calculateInnerStartPoint
        val p2 = calculateOuterStartPoint
        val p3 = calculateOuterEndPoint
        val p4 = calculateInnerEndPoint

        "M " + p1._1 + " " + p1._2 +
        " L " + p2._1 + " " + p2._2 + " " +
        " A " + radius + " " + radius + " 0 " + largeArcFlag + " " + getSweepFlag + " " + p3._1 + " " + p3._2 +
        " L " + p4._1 + " " + p4._2 +
        " A " + innerRadius + " " + innerRadius + " 0 " + largeArcFlag + " " + getInnerSweepFlat + " " + p1._1 + " " + p1._2
    }

    /**
      * @return the path (<path d=.. />) for the line connecting the value label with the segment
      */
    def calculateLabelPath: String = {
        val (px, py) = calculateOuterMiddlePoint
        val (zx, zy) = calculateValueLabelAnchorPoint

        "M " + px + " " + py + " L " + zx + " " + zy
    }

    /**
      * @return the point on the inner segment side with the start angle
      */
    def calculateInnerStartPoint: (Double, Double) = {
        Segment.calculatePoint(angleStart, innerRadius)
    }

    /**
      * @return the point on the outer segment side with the start angle
      */
    def calculateOuterStartPoint: (Double, Double) = {
        Segment.calculatePoint(angleStart, radius)
    }

    /**
      * @return the point on the inner segment side with the end angle
      */
    def calculateInnerEndPoint: (Double, Double) = {
        Segment.calculatePoint(angleEnd, innerRadius)
    }

    /**
      * @return the point on the outer segment side with the end angle
      */
    def calculateOuterEndPoint: (Double, Double) = {
        Segment.calculatePoint(angleEnd, radius)
    }

    /**
      * @return calculates the angle in the middle of the segment
      */
    def calculateAvgAngle: Double = {
        angleStart + (angleEnd - angleStart) / 2.0
    }

    /**
      * @return the point on the outer segment with the middle angle
      */
    def calculateOuterMiddlePoint: (Double, Double) = {
        Segment.calculatePoint(calculateAvgAngle, radius)
    }

    /**
      * @return the anchor point for the value label
      */
    def calculateValueLabelAnchorPoint: (Double, Double) = {
        Segment.calculatePoint(calculateAvgAngle, valueRadius)
    }

    /**
      * @return the anchor text for the value label
      */
    def calculateValueLabelAnchor: String = {
        val direction = math.signum(Segment.calculateXCoordinate(calculateAvgAngle, 1))
        if(direction < 0) "end" else "start"
    }

    /**
      * @return the large arc flag for the segment
      */
    def getLargeArcFlag: Int = {
        if(math.abs(calculateDeltaAngles) > 180) 1 else 0
    }

    /**
      * @return the sweep flag for the segment
      */
    def getSweepFlag: Int = {
        if(calculateDeltaAngles < 0) 1 else 0
    }

    /**
      * @return the inverted sweep flag used for the inner segment path
      */
    private def getInnerSweepFlat: Int = {
        if(getSweepFlag == 0) 1 else 0
    }
}
