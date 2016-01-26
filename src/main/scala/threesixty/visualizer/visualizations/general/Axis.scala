package threesixty.visualizer.visualizations.general

/**
  * @author Thomas Engel
  */
object AxisType extends Enumeration {
    val Nothing, ValueAxis, TimeAxis = Value
}

object AxisFactory {
    def createAxis(axisType: AxisType.Value,
                   availableLength: Int,
                   minValue: Double,
                   maxValue: Double,
                   axisLabel: String,
                   minDistanceBetweenGridPoints: Option[Int] = None,
                   unit: Option[String] = None): Axis = {

        val result = axisType match {
            case AxisType.Nothing => new NoAxis(availableLength,
                                                minValue,
                                                maxValue)
            case AxisType.ValueAxis => new ValueAxis(
                                                availableLength,
                                                minValue,
                                                maxValue,
                                                axisLabel,
                                                minDistanceBetweenGridPoints,
                                                convertUnitToOptDouble(unit))
            case AxisType.TimeAxis => new TimeAxis(
                                                availableLength,
                                                minValue,
                                                maxValue,
                                                axisLabel,
                                                minDistanceBetweenGridPoints,
                                                unit)
            case _ => throw new NotImplementedError("The AxisType '" + axisType.toString + "' is not implemented.")
        }

        result
    }

    private def convertUnitToOptDouble(optString: Option[String]): Option[Double] = {
        var result: Option[Double] = None
        try {
            result = Some(optString.get.toDouble)
        } catch {
            case _: Throwable =>
        }

        result
    }
}

abstract class Axis(availableLength: Int,
                    minValue: Double,
                    maxValue: Double,
                    axisLabel: String,
                    minDistanceBetweenGridPoints: Option[Int]) {

    def _minDistanceBetweenGridPoints = minDistanceBetweenGridPoints.getOrElse(50)

    def getNumberOfGridPoints: Int

    def getUnit: Double

    def getMinimumDisplayedValue: Double

    def getMaximumDisplayedValue: Double

    def convertValue(value: Double): Double

    def getGridLabels: List[String]

    def getLengthBetweenGridPoints: Double = {
        (1.0*availableLength) / getNumberOfGridPoints
    }

    def getGridPointsAndLabel: List[(Double, String)] = {
        var points: List[Double] = List.empty

        for(i <- 0 until getNumberOfGridPoints)
            points = (availableLength - i * getLengthBetweenGridPoints) :: points

        val labels = getGridLabels

        points.zip(labels)
    }

    def getAxisLabel: String = axisLabel
}

case class NoAxis(val availableLength: Int,
                  val minValue: Double,
                  val maxValue: Double) extends Axis(availableLength, minValue, maxValue, "", None) {

    def getNumberOfGridPoints: Int = 0

    def getUnit: Double = 0

    def getMinimumDisplayedValue: Double = minValue

    def getMaximumDisplayedValue: Double = maxValue

    def convertValue(value: Double): Double = {
        (value / (maxValue - minValue)) * availableLength
    }

    def getGridLabels: List[String] = List.empty
}

case class TimeAxis(val availableLength: Int,
                    val minValue: Double,
                    val maxValue: Double,
                    val axisLabel: String,
                    val minDistanceBetweenGridPoints: Option[Int],
                    val unit: Option[String]) extends Axis(availableLength, minValue, maxValue, axisLabel, minDistanceBetweenGridPoints) {

    // calculate the distance between two control points on the axis
    val _unit = unit match {
        case None => calculateUnit()
        case Some(name) => getTimeScalingByName(name)
    }
    val amountPoints = math.ceil((1.0*(maxValue - minValue)) / _unit.getTotalMillis).toInt

    // calculate xMin and xMax for the min/max displayed value
    val maximum = (math.ceil((1.0*maxValue) / _unit.getTotalMillis) * _unit.getTotalMillis).toLong
    val minimum = _unit.getRealMinimum(minValue.toLong)

    private def getPossibleTimeScaling: List[TimeScaling] = List(
        new TimeScalingMillis1, new TimeScalingMillis10, new TimeScalingMillis100,
        new TimeScalingSeconds1, new TimeScalingSeconds10, new TimeScalingSeconds30,
        new TimeScalingMinutes1, new TimeScalingMinutes10, new TimeScalingMinutes30,
        new TimeScalingHours1, new TimeScalingHours3, new TimeScalingHours6, new TimeScalingHours12,
        new TimeScalingDays1, new TimeScalingDays7,
        new TimeScalingMonths1, new TimeScalingMonths3, new TimeScalingMonths6,
        new TimeScalingYears1, new TimeScalingYears5, new TimeScalingYears10
    )

    private def getTimeScalingByName(name: String): TimeScaling = {
        val possible = getPossibleTimeScaling.filter((x: TimeScaling) => x.name.equals(name))
        if(possible.size > 0) {
            possible.head
        } else {
            throw new NoSuchElementException("No TimeScaling with the name '" + name + "' could be found.")
        }
    }

    private def calculateUnit(): TimeScaling = {
        val maxAmountPoints = availableLength / _minDistanceBetweenGridPoints
        val delta = maxValue - minValue

        for {unit <- getPossibleTimeScaling} {
            val result = (1.0*delta) / unit.getTotalMillis
            if(result <= maxAmountPoints) {
                return unit
            }
        }

        throw new UnsupportedOperationException("No scaling for the x-axis could be found.")
    }

    def getNumberOfGridPoints: Int = amountPoints

    def getUnit: Double = _unit.getTotalMillis

    def getMinimumDisplayedValue: Double = minimum

    def getMaximumDisplayedValue: Double = maximum

    def convertValue(value: Double): Double = {
        ((value - minimum) / (maximum - minimum)) * availableLength
    }

    def getGridLabels: List[String] = {
        var result: List[String] = List.empty

        for(i <- 0 until getNumberOfGridPoints)
            result = _unit.getLabel(i, (minimum + i * getUnit).toLong) :: result

        result.reverse
    }

    override def getAxisLabel: String = {
        val baseLabel = super.getAxisLabel

        if(!_unit.getUnit.isEmpty) {
            baseLabel + " (in " + _unit.getUnit + ")"
        } else {
            baseLabel
        }
    }
}

case class ValueAxis(val availableLength: Int,
                     val minValue: Double,
                     val maxValue: Double,
                     val axisLabel: String,
                     val minDistanceBetweenGridPoints: Option[Int],
                     val unit: Option[Double]) extends Axis(availableLength, minValue, maxValue, axisLabel, minDistanceBetweenGridPoints) {

    // calculate the distance between two control points on the y-axis
    var _unit = unit.getOrElse(-1.0)
    if(_unit <= 0) {
        _unit = calculateUnit()
    }
    val amountPoints = math.ceil((maxValue - minValue) / _unit).toInt

    // calculate yMin and yMax for the min/max displayed value
    val minimum = math.floor(minValue / _unit) * _unit
    val maximum = math.ceil(maxValue / _unit) * _unit

    private def calculateUnit(): Double = {
        val maxAmountPoints = availableLength / _minDistanceBetweenGridPoints
        val deltaY = maxValue - minValue
        var unit = 1.0

        var result = deltaY / unit

        // reduce amount until number of points is higher than the max allowed number of points
        while(result < maxAmountPoints) {
            unit /= 10
            result = deltaY / unit
        }

        // increase amount until number of points is lower than the max allowed number of points
        while(result > maxAmountPoints) {
            unit *= 10
            result = deltaY / unit
        }

        // the unit leads now to the number of points that is as close as possible
        // but smaller than the max allowed number of points
        unit
    }

    def getNumberOfGridPoints: Int = amountPoints

    def getUnit: Double = _unit

    def getMinimumDisplayedValue: Double = minimum

    def getMaximumDisplayedValue: Double = maximum

    def convertValue(value: Double): Double = {
        (value / (maximum - minimum)) * availableLength
    }

    def getGridLabels: List[String] = {
        var result: List[String] = List.empty

        for(i <- getMaximumDisplayedValue to getMinimumDisplayedValue by - getUnit)
            result = getLabelForValue(i) :: result

        result
    }

    private def getLabelForValue(value: Double): String = {
        if(value == 0)
            value.toInt.toString
        else if(math.abs(getUnit) < 0)
            value.toDouble.toString
        else
            value.toInt.toString
    }
}
