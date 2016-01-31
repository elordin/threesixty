package threesixty.visualizer.util

import java.sql.{Date, Timestamp}

/**
  * @author Thomas Engel
  */
abstract class TimeScaling(val name: String, val baseMillis: Long, val factor: Int = 1) {
    require(baseMillis > 0, "Value for baseMillis must be positive.")

    def getTotalMillis: Long = baseMillis * factor

    def getUnit: String = ""

    def getLabel(index: Int, millis: Long): String

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








trait Scale[T] {
    def format(value: T): String
    def nextBreakpoint(value: T): T
    def scale(value: T): Int
}

object TimeScale {
    def apply(inMin: Long, inMax: Long, outMin: Int, outMax: Int, unit: String): TimeScale = ???
    def bestFit(min: Long, max: Long): TimeScale = ???
}

case class TimeScale(inMin: Long, inMax: Long, outMin: Int, outMax: Int, step: Long) extends Scale[Long] {
    def format(t: Long): String = ???
    def nextBreakpoint(t: Long): Long = ???
    def scale(t: Long): Int = (((t - inMin).toDouble / (inMax - inMin).toDouble) * (outMax - outMin)).toInt
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
    def scale(v: Double): Int = ((v - inMin) / (inMax - inMin) * (outMax - outMin)).toInt
}

