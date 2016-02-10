package threesixty.visualizer.util

import java.sql.{Timestamp}


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
