package threesixty.visualizer

import java.sql.{Time, Date, Timestamp}

import org.scalatest.FlatSpec
import threesixty.visualizer.visualizations.LineChart._

/**
  * @author Thomas Engel
  */
class XScalingTestSpec extends FlatSpec {
    "A XScalingMillis10" should "return a correct label text" in {
        val scaling = new XScalingMillis10

        assertResult("0") {scaling.getLabel(0, 0)}
        assertResult("10") {scaling.getLabel(1, 0)}
    }

    "A XScalingMillis100" should "return the correct unit" in {
        assertResult("ms") {new XScalingMillis100().getUnit}
    }

    "A XScalingMillis1" should "return the correct minimum for x" in {
        assertResult(15) {new XScalingMillis1().getXMin(15)}
    }

    "A XScalingSeconds30" should "return a correct label text" in {
        val scaling = new XScalingSeconds30

        assertResult("0") {scaling.getLabel(0, 0)}
        assertResult("30") {scaling.getLabel(1, 0)}
    }

    "A XScalingSeconds1" should "return the correct unit" in {
        assertResult("sek") {new XScalingSeconds1().getUnit}
    }

    "A XScalingSeconds10" should "return the correct minimum for x" in {
        assertResult(42) {new XScalingSeconds10().getXMin(42)}
    }

    "A XScalingMinutes1" should "return a correct label text" in {
        val scaling = new XScalingMinutes1
        val time = new Time(8, 15, 0)
        val timestamp = new Timestamp(time.getTime)

        assertResult("8:15") {scaling.getLabel(0, timestamp.getTime)}
    }

    "A XScalingMinutes10" should "return the correct minimum for x" in {
        val time = new Time(5, 8, 0)
        val timePlus = new Time(5, 8, 23)

        assertResult(time.getTime) {new XScalingMinutes10().getXMin(timePlus.getTime)}
    }

    "A XScalingMinutes30" should "return the correct unit" in {
        assert(new XScalingMinutes30().getUnit.isEmpty)
    }

    "A XScalingHours3" should "return a correct label text" in {
        val scaling = new XScalingHours3
        val time = new Time(9, 0, 0)
        val timestamp = new Timestamp(time.getTime)

        assertResult("9:00") {scaling.getLabel(0, timestamp.getTime)}
    }

    "A XScalingHours6" should "return the correct minimum for x" in {
        val time = new Time(5, 0, 0)
        val timePlus = new Time(5, 10, 13)

        assertResult(time.getTime) {new XScalingHours6().getXMin(timePlus.getTime)}
    }

    "A XScalingHours1" should "return the correct unit" in {
        assert(new XScalingHours1().getUnit.isEmpty)
    }

    "A XScalingDays7" should "return a correct label text" in {
        val scaling = new XScalingDays7
        val date = new Date(2016, 0, 21)

        assertResult("21.1") {scaling.getLabel(0, date.getTime)}
    }

    "A XScalingDays1" should "return the corret minimum for x" in {
        val date = new Date(2015, 11, 24)
        val datePlus = new Timestamp(date.getTime + 5800)

        assertResult(date.getTime) {new XScalingDays1().getXMin(datePlus.getTime)}
    }

    "A XScalingDays1" should "return the correct unit" in {
        assert(new XScalingDays1().getUnit.isEmpty)
    }

    "A XScalingMonths1" should "return a correct label text" in {
        val scaling = new XScalingMonths1
        val date = new Date(2015, 8, 1)

        assertResult("Sep") {scaling.getLabel(0, date.getTime)}
        assertResult("Okt") {scaling.getLabel(1, 0)}
    }

    "A XScalingMonths1" should "return the correct minimum for x" in {
        val date = new Date(2015, 10, 1)
        val datePlus = new Date(2015, 10, 5)

        assertResult(date.getTime) {new XScalingMonths1().getXMin(datePlus.getTime)}
    }

    "A XScalingMonths3" should "return the correct unit" in {
        assert(new XScalingMonths3().getUnit.isEmpty)
    }

    "A XScalingYears1" should "return a correct label text" in {
        val scaling = new XScalingYears1
        val date = new Date(2004, 0, 1)

        assertResult("2004") {scaling.getLabel(0, date.getTime)}
        assertResult("2005") {scaling.getLabel(1, 0)}
    }

    "A XScalingYears5" should "return the correct minimum for x" in {
        val date = new Date(2016, 0, 1)
        val datePlus = new Date(2016, 4, 12)

        assertResult(date.getTime) {new XScalingYears5().getXMin(datePlus.getTime)}
    }

    "A XScalingYears10" should "return the correct unit" in {
        assert(new XScalingYears10().getUnit.isEmpty)
    }
}
