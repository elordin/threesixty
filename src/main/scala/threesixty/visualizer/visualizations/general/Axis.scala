package threesixty.visualizer.visualizations.general

/**
  * @author Thomas Engel
  */
object AxisType extends Enumeration {
    val Nothing, ValueAxis, TimeAxis = Value
}

/**
  * @author Thomas Engel
  */
object AxisDimension extends Enumeration {
    val xAxis, yAxis = Value
}

/**
  * @author Thomas Engel
  */
object AxisFactory {
    /**
      * Factory method to create an axis.
      *
      * @param axisType the type of the axis
      * @param axisDim the dimension of the axis
      * @param availableLength the available length for the axis
      * @param minValue the minimum value displayed on the axis
      * @param maxValue the maximum value displayed on the axis
      * @param axisLabel the label of the axis
      * @param minDistanceBetweenGridPoints the minimum distance between two grid points on the axis
      * @param unit the unit of the axis
      * @return an axis
      */
    def createAxis(axisType: AxisType.Value,
                   axisDim: AxisDimension.Value,
                   availableLength: Int,
                   minValue: Double,
                   maxValue: Double,
                   axisLabel: String = "",
                   minDistanceBetweenGridPoints: Option[Int] = None,
                   unit: Option[String] = None): Axis = {

        val result = axisType match {
            case AxisType.Nothing => new NoAxis(axisDim,
                                                availableLength,
                                                axisLabel)
            case AxisType.ValueAxis => new ValueAxis(axisDim,
                                                     availableLength,
                                                     minValue,
                                                     maxValue,
                                                     axisLabel,
                                                     minDistanceBetweenGridPoints,
                                                     convertUnitToOptDouble(unit))
            case AxisType.TimeAxis => new TimeAxis(axisDim,
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

    /**
      * Tries to convert the string to a double. If the convertion is not possible [[None]] will be returned.
      *
      * @param optString the input string
      * @return the converted string
      */
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

/**
  * Abstract class for an axis.
  *
  * @param axisDimension the dimension
  * @param availableLength the available length
  * @param axisLabel the label
  * @param minDistanceBetweenGridPoints the minimum distance between two grid points
  *
  * @author Thomas Engel
  */
abstract class Axis(axisDimension: AxisDimension.Value,
                    availableLength: Int,
                    axisLabel: String,
                    minDistanceBetweenGridPoints: Option[Int]) {

    /**
      * If the axis dimension is [[AxisDimension.yAxis]] -1 is returned.
      * Otherwise 1.
      *
      * @return the signum
      */
    def calculateSignum: Int = {
        axisDimension match {
            case AxisDimension.yAxis => -1
            case _ => 1
        }
    }

    /**
      * @return the minDistanceBetweenGridPoints or a default value
      */
    def _minDistanceBetweenGridPoints = minDistanceBetweenGridPoints.getOrElse(50)

    /**
      * @return the number of grid points
      */
    def getNumberOfGridPoints: Int

    /**
      * @return the unit
      */
    def getUnit: Double

    /**
      * @return the minimum value that is displayed
      */
    def getMinimumDisplayedValue: Double

    /**
      * @return the maximum value that is displayed
      */
    def getMaximumDisplayedValue: Double

    /**
      * Converts the value to the axis but does not consider the signum.
      *
      * @param value value to convert
      * @return the converted value
      */
    protected def convertValue(value: Double): Double

    /**
      * Converts the value to the axis and considers the signum.
      * So the returned value equates the point on the axis.
      *
      * @param value value to convert
      * @return the value on the axis
      */
    def convert(value: Double): Double = {
        calculateSignum * convertValue(value)
    }

    /**
      * @return the list of grid labels
      */
    def getGridLabels: List[String]

    /**
      * @return the actual distance between two grid points
      */
    def getLengthBetweenGridPoints: Double = {
        (1.0*availableLength) / (getNumberOfGridPoints - 1)
    }

    /**
      * @return a list containing the unit value as well as the grid label
      */
    def getGridPointsAndLabel: List[(Double, String)] = {
        var points: List[Double] = List.empty
        var minimum = convertValue(getMinimumDisplayedValue)

        for(i <- 0 until getNumberOfGridPoints) {
            points = (calculateSignum * (minimum + availableLength - i * getLengthBetweenGridPoints)) :: points
        }

        val labels = getGridLabels

        points.zip(labels)
    }

    /**
      * @return the label of the axis
      */
    def getAxisLabel: String = axisLabel
}

/**
  * Represents an axis without any grid points but a label can be added.
  *
  * @param axisDimension the dimension
  * @param availableLength the available length
  * @param axisLabel the label
  */
case class NoAxis(val axisDimension: AxisDimension.Value,
                  val availableLength: Int,
                  val axisLabel: String) extends Axis(axisDimension, availableLength, axisLabel, None) {

    /**
      * @return the number of grid points
      */
    def getNumberOfGridPoints: Int = 0

    /**
      * @return the unit
      */
    def getUnit: Double = 0

    /**
      * @return the minimum value that is displayed
      */
    def getMinimumDisplayedValue: Double = 0

    /**
      * @return the maximum value that is displayed
      */
    def getMaximumDisplayedValue: Double = availableLength

    /**
      * @param value value to convert
      * @return the converted value
      */
    def convertValue(value: Double): Double = value

    /**
      * @return the list of grid labels
      */
    def getGridLabels: List[String] = List.empty
}

/**
  * Represents an axis with a time scaling.
  * The desired unit can be one of the following:
  * - millis1, millis10, millis100
  * - seconds1, seconds10, seconds30
  * - minutes1, minutes10, minutes30
  * - hours1, hours3, hours6, hours12
  * - days1, days7
  * - months1, months3, months6
  * - years1, years5, years10
  *
  * @param axisDimension the dimension
  * @param availableLength the available length
  * @param minValue the minimum value
  * @param maxValue the maximum value
  * @param axisLabel the label
  * @param minDistanceBetweenGridPoints the minimum distance between two grid points
  * @param unit the desired unit
  */
case class TimeAxis(val axisDimension: AxisDimension.Value,
                    val availableLength: Int,
                    val minValue: Double,
                    val maxValue: Double,
                    val axisLabel: String,
                    val minDistanceBetweenGridPoints: Option[Int],
                    val unit: Option[String]) extends Axis(axisDimension, availableLength, axisLabel, minDistanceBetweenGridPoints) {

    // calculate the distance between two control points on the axis
    val _unit = unit match {
        case None => calculateUnit()
        case Some(name) => getTimeScalingByName(name)
    }
    // calculate maximum and minimum for the min/max displayed value
    val maximum = (math.ceil((1.0*maxValue) / _unit.getTotalMillis) * _unit.getTotalMillis).toLong
    val minimum = _unit.getRealMinimum(minValue.toLong)

    val amountPoints = math.ceil((1.0*(maxValue - minValue)) / _unit.getTotalMillis).toInt

    /**
      * @return a list of possible [[TimeScaling]]s
      */
    private def getPossibleTimeScaling: List[TimeScaling] = List(
        new TimeScalingMillis1, new TimeScalingMillis10, new TimeScalingMillis100,
        new TimeScalingSeconds1, new TimeScalingSeconds10, new TimeScalingSeconds30,
        new TimeScalingMinutes1, new TimeScalingMinutes10, new TimeScalingMinutes30,
        new TimeScalingHours1, new TimeScalingHours3, new TimeScalingHours6, new TimeScalingHours12,
        new TimeScalingDays1, new TimeScalingDays7,
        new TimeScalingMonths1, new TimeScalingMonths3, new TimeScalingMonths6,
        new TimeScalingYears1, new TimeScalingYears5, new TimeScalingYears10
    )

    /**
      * @param name the name of the [[TimeScaling]]
      * @return the [[TimeScaling]] with the given name
      */
    @throws[NoSuchElementException]("if there is no time scaling with the given name.")
    private def getTimeScalingByName(name: String): TimeScaling = {
        val possible = getPossibleTimeScaling.filter((x: TimeScaling) => x.name.equals(name))
        if(possible.size > 0) {
            possible.head
        } else {
            throw new NoSuchElementException("No TimeScaling with the name '" + name + "' could be found.")
        }
    }

    /**
      * @return the suitable [[TimeScaling]] for the axis
      */
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

    /**
      * @return the number of grid points
      */
    def getNumberOfGridPoints: Int = amountPoints + 1

    /**
      * @return the unit
      */
    def getUnit: Double = _unit.getTotalMillis

    /**
      * @return the minimum value that is displayed
      */
    def getMinimumDisplayedValue: Double = minimum

    /**
      * @return the maximum value that is displayed
      */
    def getMaximumDisplayedValue: Double = maximum

    /**
      * @param value value to convert
      * @return the converted value
      */
    def convertValue(value: Double): Double = {
        ((value - minimum) / (maximum - minimum)) * availableLength
    }

    /**
      * @return the list of grid labels
      */
    def getGridLabels: List[String] = {
        var result: List[String] = List.empty

        for(i <- 0 until getNumberOfGridPoints)
            result = _unit.getLabel(i, (minimum + i * getUnit).toLong) :: result

        result.reverse
    }

    /**
      * @return the label of the axis
      */
    override def getAxisLabel: String = {
        val baseLabel = super.getAxisLabel

        if(!_unit.getUnit.isEmpty) {
            baseLabel + " (in " + _unit.getUnit + ")"
        } else {
            baseLabel
        }
    }
}

/**
  * Represents an axis with a value scaling.
  *
  * @param axisDimension the dimension
  * @param availableLength the available length
  * @param minValue the minimum value
  * @param maxValue the maximum value
  * @param axisLabel the label
  * @param minDistanceBetweenGridPoints the minimum distance between two grid points
  * @param unit the desired unit
  */
case class ValueAxis(val axisDimension: AxisDimension.Value,
                     val availableLength: Int,
                     val minValue: Double,
                     val maxValue: Double,
                     val axisLabel: String,
                     val minDistanceBetweenGridPoints: Option[Int],
                     val unit: Option[Double]) extends Axis(axisDimension, availableLength, axisLabel, minDistanceBetweenGridPoints) {

    // calculate the distance between two control points on the y-axis
    var _unit = unit.getOrElse(-1.0)
    if(_unit <= 0) {
        _unit = calculateUnit()
    }
    // calculate minimum and maximum for the min/max displayed value
    val minimum = math.floor(minValue / _unit) * _unit
    val maximum = math.ceil(maxValue / _unit) * _unit

    val amountPoints = math.ceil((maximum - minimum) / _unit).toInt

    /**
      * @return the calculated unit
      */
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

    /**
      * @return the number of grid points
      */
    def getNumberOfGridPoints: Int = amountPoints + 1

    /**
      * @return the unit
      */
    def getUnit: Double = _unit

    /**
      * @return the minimum value that is displayed
      */
    def getMinimumDisplayedValue: Double = minimum

    /**
      * @return the maximum value that is displayed
      */
    def getMaximumDisplayedValue: Double = maximum

    /**
      * @param value value to convert
      * @return the converted value
      */
    def convertValue(value: Double): Double = {
        (value / (maximum - minimum)) * availableLength
    }

    /**
      * @return the list of grid labels
      */
    def getGridLabels: List[String] = {
        var result: List[String] = List.empty

        for(i <- getMaximumDisplayedValue to getMinimumDisplayedValue by - getUnit)
            result = getLabelForValue(i) :: result

        result
    }

    /**
      * @param value the value
      * @return the label string for the given value
      */
    private def getLabelForValue(value: Double): String = {
        if(value == 0)
            value.toInt.toString
        else if(math.abs(getUnit) < 0)
            value.toDouble.toString
        else
            value.toInt.toString
    }
}
