package threesixty.visualizer.util

import java.sql.{Date, Timestamp}

/*

/**
  * This abstract class is used for scaling a time period.
  *
  * @param name the name
  * @param baseMillis the milliseconds for the base unit
  * @param factor the factor
  *
  * @author Thomas Engel
  */
abstract class TimeScaling(val name: String, val baseMillis: Long, val factor: Int = 1) {
    require(baseMillis > 0, "Value for baseMillis must be positive.")

    /**
      * @return the total number of milliseconds for this scaling
      */
    def getTotalMillis: Long = baseMillis * factor

    /**
      * @return a string for the unit
      */
    def getUnit: String = ""

    /**
      * @param index of the point starting with 0
      * @param millis the milliseconds at this point
      * @return the label string
      */
    def getLabel(index: Int, millis: Long): String

    /**
      * @param currentXMin the desired minimum at the start
      * @return the actual minimum displayed value
      */
    def getRealMinimum(currentXMin: Long): Long
}


abstract class TimeScalingMillis(name: String, factor: Int) extends TimeScaling(name, 1L, factor) {
    def getLabel(index: Int, millis: Long) = {
        "" + (index * factor)
    }

    def getRealMinimum(currentXMin: Long): Long = {
        currentXMin
    }

    override def getUnit = "ms"
}
case class TimeScalingMillis1() extends TimeScalingMillis("millis1", 1)
case class TimeScalingMillis10() extends TimeScalingMillis("millis10", 10)
case class TimeScalingMillis100() extends TimeScalingMillis("millis100", 100)


abstract class TimeScalingSeconds(name: String, factor: Int) extends TimeScaling(name, 1000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        "" + (index * factor)
    }

    def getRealMinimum(currentXMin: Long): Long = {
        currentXMin
    }

    override def getUnit = "sek"
}
case class TimeScalingSeconds1() extends TimeScalingSeconds("seconds1", 1)
case class TimeScalingSeconds10() extends TimeScalingSeconds("seconds10", 10)
case class TimeScalingSeconds30() extends TimeScalingSeconds("seconds30", 30)


abstract class TimeScalingMinutes(name: String, factor: Int) extends TimeScaling(name, 60000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        val timestamp = new Timestamp(millis)
        "" + timestamp.getHours + ":" + (if(timestamp.getMinutes < 10) "0" else "") + timestamp.getMinutes
    }

    def getRealMinimum(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val minutes = timestamp.getMinutes
        val hours = timestamp.getHours
        val day = timestamp.getDate
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month , day)
        date.getTime + (minutes + 60 * hours) * baseMillis
    }
}
case class TimeScalingMinutes1() extends TimeScalingMinutes("minutes1", 1)
case class TimeScalingMinutes10() extends TimeScalingMinutes("minutes10", 10)
case class TimeScalingMinutes30() extends TimeScalingMinutes("minutes30", 30)


abstract class TimeScalingHours(name: String, factor: Int) extends TimeScaling(name, 3600000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        val timestamp = new Timestamp(millis)
        "" + timestamp.getHours + ":" + (if(timestamp.getMinutes < 10) "0" else "") + timestamp.getMinutes
    }

    def getRealMinimum(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val hours = timestamp.getHours
        val day = timestamp.getDate
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month , day)
        date.getTime + hours * baseMillis
    }
}
case class TimeScalingHours1() extends TimeScalingHours("hours1", 1)
case class TimeScalingHours3() extends TimeScalingHours("hours3", 3)
case class TimeScalingHours6() extends TimeScalingHours("hours6", 6)
case class TimeScalingHours12() extends TimeScalingHours("hours12", 12)


abstract class TimeScalingDays(name: String, factor: Int) extends TimeScaling(name, 86400000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        val timestamp = new Timestamp(millis)
        "" + timestamp.getDate + "." + (timestamp.getMonth + 1)
    }

    def getRealMinimum(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val day = timestamp.getDate
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month, day)
        date.getTime
    }
}
case class TimeScalingDays1() extends TimeScalingDays("days1", 1)
case class TimeScalingDays7() extends TimeScalingDays("days7", 7)


abstract class TimeScalingMonths(name: String, factor: Int) extends TimeScaling(name, 2628000000L, factor) {
    var lastStartValue = new Timestamp(0)

    def getLabel(index: Int, millis: Long) = {
        if(index == 0) {
            lastStartValue = new Timestamp(millis)
        }

        (lastStartValue.getMonth + index * factor) % 12 match {
            case 0 => "Jan"
            case 1 => "Feb"
            case 2 => "Mrz"
            case 3 => "Apr"
            case 4 => "Mai"
            case 5 => "Jun"
            case 6 => "Jul"
            case 7 => "Aug"
            case 8 => "Sep"
            case 9 => "Okt"
            case 10 => "Nov"
            case 11 => "Dez"
            case _ => "Unbek"
        }
    }

    def getRealMinimum(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month, 1)
        date.getTime
    }
}
case class TimeScalingMonths1() extends TimeScalingMonths("months1", 1)
case class TimeScalingMonths3() extends TimeScalingMonths("months3", 3)
case class TimeScalingMonths6() extends TimeScalingMonths("months6", 6)


abstract class TimeScalingYears(name: String, factor: Int) extends TimeScaling(name, 31540000000L, factor) {
    var lastStartValue = new Timestamp(0)

    def getLabel(index: Int, millis: Long) = {
        if(index == 0) {
            lastStartValue = new Timestamp(millis)
        }

        "" + (lastStartValue.getYear + index * factor)
    }

    def getRealMinimum(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val year = timestamp.getYear
        val date = new Date(year, 0, 1)
        date.getTime
    }
}
case class TimeScalingYears1() extends TimeScalingYears("years1", 1)
case class TimeScalingYears5() extends TimeScalingYears("years5", 5)
case class TimeScalingYears10() extends TimeScalingYears("years10", 10)



*/



trait Scale[T] {
    def format(value: T): String
    def nextBreakpoint(value: T): T
    def apply(value: T): Long
}

object TimeScale {
    object TimeUnits extends Enumeration {
        class TimeUnit(val i: Int, val name: String, val millis: Long, val shortName: String) extends Val(i: Int, name: String)
        val MILLISECONDS = new TimeUnit(0, "milliseconds", 1, "ms")
        val SECONDS      = new TimeUnit(1, "seconds", 1000L, "s")
        val MINUTES      = new TimeUnit(2, "minutes", 60000L, "min")
        val HOURS        = new TimeUnit(3, "hours", 3600000L, "h")
        val DAYS         = new TimeUnit(4, "days", 86400000L, "")
        val WEEKS        = new TimeUnit(5, "weeks", 604800000L, "")
        val MONTHS       = new TimeUnit(6, "months", 2628000000L, "")
        val YEARS        = new TimeUnit(7, "years", 31540000000L, "")
    }
    import TimeUnits._

    val AVAILABLE_SCALE_UNITS = Seq(
        (MILLISECONDS, 1), (MILLISECONDS, 10), (MILLISECONDS, 100),
        (SECONDS, 1), (SECONDS, 10),
        (MINUTES, 1), (MINUTES, 10),
        (HOURS, 1), (HOURS, 12), (HOURS, 24),
        (DAYS, 1), (DAYS, 10),
        (WEEKS, 1),
        (MONTHS, 1), (MONTHS, 10),
        (YEARS, 1)
    )

    @throws[NumberFormatException]("if the given unit string is invalid")
    def apply(inMin: Long, inMax: Long, outMin: Int, outMax: Int, unparsedStep: String): TimeScale = {
        val matchingRegEx = " *(0*[1-9][0-9]*) *(milli|millis|second|seconds|minute|minutes|hour|hours|day|days|week|weeks|month|months|year|years) *".r
        val (stepSize, unit) = unparsedStep match {
            case matchingRegEx(s, "milli")   => (s, MILLISECONDS)
            case matchingRegEx(s, "millis")  => (s, MILLISECONDS)
            case matchingRegEx(s, "second")  => (s, SECONDS)
            case matchingRegEx(s, "seconds") => (s, SECONDS)
            case matchingRegEx(s, "minute")  => (s, MINUTES)
            case matchingRegEx(s, "minutes") => (s, MINUTES)
            case matchingRegEx(s, "hour")    => (s, HOURS)
            case matchingRegEx(s, "hours")   => (s, HOURS)
            case matchingRegEx(s, "day")     => (s, DAYS)
            case matchingRegEx(s, "days")    => (s, DAYS)
            case matchingRegEx(s, "week")    => (s, WEEKS)
            case matchingRegEx(s, "weeks")   => (s, WEEKS)
            case matchingRegEx(s, "month")   => (s, MONTHS)
            case matchingRegEx(s, "months")  => (s, MONTHS)
            case matchingRegEx(s, "year")    => (s, YEARS)
            case matchingRegEx(s, "years")   => (s, YEARS)
            case _ => throw new NumberFormatException("Invalid format for scale step.")
        }
        TimeScale(inMin, inMax, outMin, outMax, unit, stepSize.toLong)
    }

    def apply(inMin: Long, inMax: Long, outMin: Int, outMax: Int): TimeScale = {
        val (unit: TimeUnit, stepSize: Long) = (for { (unit, steps) <- AVAILABLE_SCALE_UNITS } yield {
            val stepSize: Long = unit.millis * steps
            (stepSize, (inMax - inMin) / stepSize, unit)
        }).dropWhile(_._2 > 25).headOption.map({
            case (stepSize, stepCount, unit) => (unit, stepSize)}).getOrElse((YEARS, 1))
        TimeScale(inMin, inMax, outMin, outMax, unit, stepSize)
    }
}

case class TimeScale(inMin: Long, inMax: Long, outMin: Int, outMax: Int, unit: TimeScale.TimeUnits.TimeUnit, step: Long) extends Scale[Long] {
    import TimeScale.TimeUnits._

    def format(t: Long): String = unit match {
        case MILLISECONDS => "+" + (t - inMin) + "ms"
        case SECONDS => "+" + ((t - inMin) / 1000) + "s"
        case MINUTES => { // TODO
            val time = new Timestamp(t)
            val h = time.getHours
            val min = time.getMinutes
            val m = if (min < 10) ("0" + min) else min.toString
            s"$h:$m Uhr"
        }
        case HOURS => (new Timestamp(t)).getHours + " Uhr"
        case DAYS => { // TODO
            val time = new Timestamp(t)
            val d = time.getDate
            val m = time.getMonth + 1
            s"$d.$m."
        }
        case WEEKS => "" // TODO
        case MONTHS => "" // TODO
        case YEARS => "" // TODO
    }
    def nextBreakpoint(t: Long): Long = t - (t % step) + step
    def apply(t: Long): Long = (((t - inMin).toDouble / (inMax - inMin).toDouble) * (outMax - outMin)).toLong
}

object ValueScale {
    def apply(inMin: Double, inMax: Double, outMin: Int, outMax: Int): ValueScale = {
        val stepSize: Int = (for { e <- 0 to 6 } yield {
            val stepSize: Int = math.pow(10, e).toInt
            (stepSize, (inMax - inMin) / stepSize)
        }).dropWhile(_._2 > 25).headOption.map(_._1).getOrElse(10000000)

        ValueScale(inMin, inMax, outMin, outMax, stepSize.toDouble)
    }
}

case class ValueScale(inMin: Double, inMax: Double, outMin: Int, outMax: Int, step: Double) extends Scale[Double] {
    def format(v: Double): String = v.toString
    def nextBreakpoint(v: Double): Double = v - (v % step) + step
    def apply(v: Double): Long = ((v - inMin) / (inMax - inMin) * (outMax - outMin)).toLong
}
