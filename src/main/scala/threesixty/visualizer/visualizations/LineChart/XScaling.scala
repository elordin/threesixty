package threesixty.visualizer.visualizations.LineChart

import java.sql.{Date, Timestamp}

/**
  * @author Thomas Engel
  */
abstract class XScaling(val name: String, val baseMillis: Long, val factor: Int = 1) {
    require(baseMillis > 0, "Value for baseMillis must be positive.")

    def getTotalMillis: Long = baseMillis * factor

    def getUnit: String = ""

    def getLabel(index: Int, millis: Long): String

    def getXMin(currentXMin: Long): Long
}


abstract class XScalingMillis(name: String, factor: Int) extends XScaling(name, 1L, factor) {
    def getLabel(index: Int, millis: Long) = {
        "" + (index * factor)
    }

    def getXMin(currentXMin: Long): Long = {
        currentXMin
    }

    override def getUnit = "ms"
}
case class XScalingMillis1() extends XScalingMillis("millis1", 1)
case class XScalingMillis10() extends XScalingMillis("millis10", 10)
case class XScalingMillis100() extends XScalingMillis("millis100", 100)


abstract class XScalingSeconds(name: String, factor: Int) extends XScaling(name, 1000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        "" + (index * factor)
    }

    def getXMin(currentXMin: Long): Long = {
        currentXMin
    }

    override def getUnit = "sek"
}
case class XScalingSeconds1() extends XScalingSeconds("seconds1", 1)
case class XScalingSeconds10() extends XScalingSeconds("seconds10", 10)
case class XScalingSeconds30() extends XScalingSeconds("seconds30", 30)


abstract class XScalingMinutes(name: String, factor: Int) extends XScaling(name, 60000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        val timestamp = new Timestamp(millis)
        "" + timestamp.getHours + ":" + (if(timestamp.getMinutes < 10) "0" else "") + timestamp.getMinutes
    }

    def getXMin(currentXMin: Long): Long = {
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
case class XScalingMinutes1() extends XScalingMinutes("minutes1", 1)
case class XScalingMinutes10() extends XScalingMinutes("minutes10", 10)
case class XScalingMinutes30() extends XScalingMinutes("minutes30", 30)


abstract class XScalingHours(name: String, factor: Int) extends XScaling(name, 3600000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        val timestamp = new Timestamp(millis)
        "" + timestamp.getHours + ":" + (if(timestamp.getMinutes < 10) "0" else "") + timestamp.getMinutes
    }

    def getXMin(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val hours = timestamp.getHours
        val day = timestamp.getDate
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month , day)
        date.getTime + hours * baseMillis
    }
}
case class XScalingHours1() extends XScalingHours("hours1", 1)
case class XScalingHours3() extends XScalingHours("hours3", 3)
case class XScalingHours6() extends XScalingHours("hours6", 6)
case class XScalingHours12() extends XScalingHours("hours12", 12)


abstract class XScalingDays(name: String, factor: Int) extends XScaling(name, 86400000L, factor) {
    def getLabel(index: Int, millis: Long) = {
        val timestamp = new Timestamp(millis)
        "" + timestamp.getDate + "." + (timestamp.getMonth + 1)
    }

    def getXMin(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val day = timestamp.getDate
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month, day)
        date.getTime
    }
}
case class XScalingDays1() extends XScalingDays("days1", 1)
case class XScalingDays7() extends XScalingDays("days7", 7)


abstract class XScalingMonths(name: String, factor: Int) extends XScaling(name, 2628000000L, factor) {
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

    def getXMin(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val month = timestamp.getMonth
        val year = timestamp.getYear
        val date = new Date(year, month, 1)
        date.getTime
    }
}
case class XScalingMonths1() extends XScalingMonths("months1", 1)
case class XScalingMonths3() extends XScalingMonths("months3", 3)
case class XScalingMonths6() extends XScalingMonths("months6", 6)


abstract class XScalingYears(name: String, factor: Int) extends XScaling(name, 31540000000L, factor) {
    var lastStartValue = new Timestamp(0)

    def getLabel(index: Int, millis: Long) = {
        if(index == 0) {
            lastStartValue = new Timestamp(millis)
        }

        "" + (lastStartValue.getYear + index * factor)
    }

    def getXMin(currentXMin: Long): Long = {
        val timestamp = new Timestamp(currentXMin)
        val year = timestamp.getYear
        val date = new Date(year, 0, 1)
        date.getTime
    }
}
case class XScalingYears1() extends XScalingYears("years1", 1)
case class XScalingYears5() extends XScalingYears("years5", 5)
case class XScalingYears10() extends XScalingYears("years10", 10)

























