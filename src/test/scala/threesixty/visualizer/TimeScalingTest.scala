package threesixty.visualizer

import java.sql.{Time, Date, Timestamp}

import org.scalatest.FlatSpec
import threesixty.visualizer.visualizations.LineChart._
import threesixty.visualizer.visualizations.general._

/**
  * @author Thomas Engel
  */
class XScalingTestSpec extends FlatSpec {
    "A XScalingMillis10" should "return a correct label text" in {
        val scaling = new TimeScalingMillis10

        assertResult("0") {scaling.getLabel(0, 0)}
        assertResult("10") {scaling.getLabel(1, 0)}
    }

    "A XScalingMillis100" should "return the correct unit" in {
        assertResult("ms") {new TimeScalingMillis100().getUnit}
    }

    "A XScalingMillis1" should "return the correct minimum for x" in {
        assertResult(15) {new TimeScalingMillis1().getRealMinimum(15)}
    }

    "A XScalingSeconds30" should "return a correct label text" in {
        val scaling = new TimeScalingSeconds30

        assertResult("0") {scaling.getLabel(0, 0)}
        assertResult("30") {scaling.getLabel(1, 0)}
    }

    "A XScalingSeconds1" should "return the correct unit" in {
        assertResult("sek") {new TimeScalingSeconds1().getUnit}
    }

    "A XScalingSeconds10" should "return the correct minimum for x" in {
        assertResult(42) {new TimeScalingSeconds10().getRealMinimum(42)}
    }

    "A XScalingMinutes1" should "return a correct label text" in {
        val scaling = new TimeScalingMinutes1
        val time = new Time(8, 15, 0)
        val timestamp = new Timestamp(time.getTime)

        assertResult("8:15") {scaling.getLabel(0, timestamp.getTime)}
    }

    "A XScalingMinutes10" should "return the correct minimum for x" in {
        val time = new Time(5, 8, 0)
        val timePlus = new Time(5, 8, 23)

        assertResult(time.getTime) {new TimeScalingMinutes10().getRealMinimum(timePlus.getTime)}
    }

    "A XScalingMinutes30" should "return the correct unit" in {
        assert(new TimeScalingMinutes30().getUnit.isEmpty)
    }

    "A XScalingHours3" should "return a correct label text" in {
        val scaling = new TimeScalingHours3
        val time = new Time(9, 0, 0)
        val timestamp = new Timestamp(time.getTime)

        assertResult("9:00") {scaling.getLabel(0, timestamp.getTime)}
    }

    "A XScalingHours6" should "return the correct minimum for x" in {
        val time = new Time(5, 0, 0)
        val timePlus = new Time(5, 10, 13)

        assertResult(time.getTime) {new TimeScalingHours6().getRealMinimum(timePlus.getTime)}
    }

    "A XScalingHours1" should "return the correct unit" in {
        assert(new TimeScalingHours1().getUnit.isEmpty)
    }

    "A XScalingDays7" should "return a correct label text" in {
        val scaling = new TimeScalingDays7
        val date = new Date(2016, 0, 21)

        assertResult("21.1") {scaling.getLabel(0, date.getTime)}
    }

    "A XScalingDays1" should "return the corret minimum for x" in {
        val date = new Date(2015, 11, 24)
        val datePlus = new Timestamp(date.getTime + 5800)

        assertResult(date.getTime) {new TimeScalingDays1().getRealMinimum(datePlus.getTime)}
    }

    "A XScalingDays1" should "return the correct unit" in {
        assert(new TimeScalingDays1().getUnit.isEmpty)
    }

    "A XScalingMonths1" should "return a correct label text" in {
        val scaling = new TimeScalingMonths1
        val date = new Date(2015, 8, 1)

        assertResult("Sep") {scaling.getLabel(0, date.getTime)}
        assertResult("Okt") {scaling.getLabel(1, 0)}
    }

    "A XScalingMonths1" should "return the correct minimum for x" in {
        val date = new Date(2015, 10, 1)
        val datePlus = new Date(2015, 10, 5)

        assertResult(date.getTime) {new TimeScalingMonths1().getRealMinimum(datePlus.getTime)}
    }

    "A XScalingMonths3" should "return the correct unit" in {
        assert(new TimeScalingMonths3().getUnit.isEmpty)
    }

    "A XScalingYears1" should "return a correct label text" in {
        val scaling = new TimeScalingYears1
        val date = new Date(2004, 0, 1)

        assertResult("2004") {scaling.getLabel(0, date.getTime)}
        assertResult("2005") {scaling.getLabel(1, 0)}
    }

    "A XScalingYears5" should "return the correct minimum for x" in {
        val date = new Date(2016, 0, 1)
        val datePlus = new Date(2016, 4, 12)

        assertResult(date.getTime) {new TimeScalingYears5().getRealMinimum(datePlus.getTime)}
    }

    "A XScalingYears10" should "return the correct unit" in {
        assert(new TimeScalingYears10().getUnit.isEmpty)
    }
}
