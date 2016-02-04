package threesixty.data.metadata

import java.util.UUID

import threesixty.data.{DataPoint, InputData}
import threesixty.data.Data.Timestamp

import org.scalatest.FlatSpec



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
        val inputdata = InputData("Data1", "", datapoints, null)

        val timeframe = Timeframe.deduceInputData(inputdata.dataPoints)

        assertResult(min) {timeframe.start}
        assertResult(max) {timeframe.end}
    }

    "A Timeframe" should "deduce the time frame from a list of input data" in {
        val min = new Timestamp(300)
        val max = new Timestamp(1500)

        val datapoints1 = List(DataPoint(min, 1), DataPoint(new Timestamp(700), 2))
        val datapoints2 = List(DataPoint(new Timestamp(500), 1), DataPoint(new Timestamp(1000), 2))
        val datapoints3 = List(DataPoint(new Timestamp(900), 1), DataPoint(max, 2))

        val metadata = CompleteInputMetadata(
                Timeframe(new Timestamp(0), new Timestamp(1)),
                Reliability.Unknown,
                Resolution.Low,
                Scaling.Ordinal,
                ActivityType("something"),
                2
            )

        val inputdata = List(
            InputData("Data1", "", datapoints1, metadata),
            InputData("Data2", "", datapoints2, metadata),
            InputData("Data3", "", datapoints3, metadata))

        val timeframe = Timeframe.deduceInputData(inputdata)

        assertResult(min) {timeframe.start}
        assertResult(max) {timeframe.end}
    }
}
