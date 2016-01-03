package threesixty.metadata

import java.sql.Timestamp

import org.scalatest.FlatSpec
import threesixty.data.{DataPoint, InputData}

/**
  * @author Thomas Engel
  */
class TimeframeTestSpec extends FlatSpec{

    "A Timeframe" should "deduce the time frame from a input data" in {
        val min = new Timestamp(200)
        val max = new Timestamp(1200)

        val datapoints = List(
            DataPoint(min, 1),
            DataPoint(new Timestamp(600), 2),
            DataPoint(max, 3))
        val inputdata = InputData(1, datapoints, null)

        val timeframe = Timeframe.deduce(inputdata)

        assertResult(min) {timeframe.start}
        assertResult(max) {timeframe.end}
    }

    "A Timeframe" should "deduce the time frame from a list of input data" in {
        val min = new Timestamp(300)
        val max = new Timestamp(1500)

        val datapoints1 = List(DataPoint(min, 1), DataPoint(new Timestamp(700), 2))
        val datapoints2 = List(DataPoint(new Timestamp(500), 1), DataPoint(new Timestamp(1000), 2))
        val datapoints3 = List(DataPoint(new Timestamp(900), 1), DataPoint(max, 2))

        val inputdata = List(
            InputData(1, datapoints1, null),
            InputData(2, datapoints2, null),
            InputData(3, datapoints3, null))

        val timeframe = Timeframe.deduce(inputdata)

        assertResult(min) {timeframe.start}
        assertResult(max) {timeframe.end}
    }
}
