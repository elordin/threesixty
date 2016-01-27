package threesixty.visualizer.visualizations.general

import scala.xml.Elem

/**
  * @author Thomas Engel
  */
object Segment {
    def calculateXCoordinate(angle: Double, radius: Double): Double = {
        radius * math.cos(math.toRadians(angle))
    }

    def calculateYCoordinate(angle: Double, radius: Double): Double = {
        - radius * math.sin(math.toRadians(angle))
    }

    def calculatePoint(angle: Double, radius: Double): (Double, Double) = {
        (calculateXCoordinate(angle, radius), calculateYCoordinate(angle, radius))
    }

    def isAngleContained(query: Double, start: Double, end: Double): Boolean = {
        (start <= query && query <= end) || (end <= query && query <= start)
    }
}

/**
  * @author Thomas Engel
  */
case class Segment(val id: String,
                   val description: String,
                   val angleStart: Double,
                   val angleEnd: Double,
                   val radius: Double,
                   val innerRadius: Double,
                   val valueRadius: Double,
                   val value: String,
                   val fontSize: Option[Int] = None,
                   val color: Option[String] = None) {

    def getSVGElement: Elem = {
        val (tlpx, tlpy) = calculateTextLabelPoint

        <g id={id}>
            <path id="segment"
                  fill={getColor}
                  d={calculatePath} />
            <path stroke="#000000"
                  d={calculateLabelPath}/>
            <text x={tlpx.toString}
                  y={tlpy.toString}
                  font-family="Roboto, Segoe UI"
                  font-weight="100"
                  font-size={getFontSize}
                  text-anchor={calculateTextAnchor}>{value}</text>
        </g>
    }

    def getColor: String = {
        if(color.isDefined) color.get else ""
    }

    def getFontSize: String = {
        if(fontSize.isDefined) fontSize.get.toString else ""
    }

    def calculateDeltaAngles: Double = {
        angleEnd - angleStart
    }

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

    def calculateLabelPath: String = {
        val (px, py) = calculateOuterMiddlePoint
        val (zx, zy) = calculateTextLabelPoint

        "M " + px + " " + py + " L " + zx + " " + zy
    }

    def calculateInnerStartPoint: (Double, Double) = {
        Segment.calculatePoint(angleStart, innerRadius)
    }

    def calculateOuterStartPoint: (Double, Double) = {
        Segment.calculatePoint(angleStart, radius)
    }

    def calculateInnerEndPoint: (Double, Double) = {
        Segment.calculatePoint(angleEnd, innerRadius)
    }

    def calculateOuterEndPoint: (Double, Double) = {
        Segment.calculatePoint(angleEnd, radius)
    }

    def calculateAvgAngle: Double = {
        angleStart + (angleEnd - angleStart) / 2.0
    }

    def calculateOuterMiddlePoint: (Double, Double) = {
        Segment.calculatePoint(calculateAvgAngle, radius)
    }

    def calculateTextLabelPoint: (Double, Double) = {
        Segment.calculatePoint(calculateAvgAngle, valueRadius)
    }

    def calculateTextAnchor: String = {
        val direction = math.signum(Segment.calculateXCoordinate(calculateAvgAngle, 1))
        if(direction < 0) "end" else "start"
    }

    def getLargeArcFlag: Int = {
        if(math.abs(calculateDeltaAngles) > 180) 1 else 0
    }

    def getSweepFlag: Int = {
        if(calculateDeltaAngles < 0) 1 else 0
    }

    private def getInnerSweepFlat: Int = {
        if(getSweepFlag == 0) 1 else 0
    }
}
