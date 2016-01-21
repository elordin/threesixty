package threesixty.visualizer.visualizations.LineChart

import threesixty.data.Data.Timestamp
import org.scalatest._


class LineChartConversionTestSpec extends FunSpec {

    describe("""Converting a String as JSON to a LineChartConfig""") {
            val jsonString = """{
                    "ids": ["abc", "123"],
                    "height": 1024,
                    "width": 768,
                    "optXMin": 100000,
                    "optXMax": 200000,
                    "optYMin": 10.0,
                    "optYMax": 123.456,
                    "xLabel": "X-Axis",
                    "yLabel": "Y-Axis",
                    "title": "Title",
                    "borderTop": 100,
                    "borderBottom": 50,
                    "borderLeft": 50,
                    "borderRight": 50,
                    "minDistanceX": 50,
                    "minDistanceY": 50,
                    "optUnitX": "seconds30",
                    "optUnitY": 10.0
                }"""

        it("should have the values set correctly") {
            val expectedResult = LineChartConfig(
                ids = Set("abc", "123"),
                height = 1024,
                width = 768,
                optXMin = Some(new Timestamp(100000)),
                optXMax = Some(new Timestamp(200000)),
                optYMin = Some(10.0),
                optYMax = Some(123.456),
                xLabel = "X-Axis",
                yLabel = "Y-Axis",
                title = "Title",
                borderTop = 100,
                borderBottom = 50,
                borderLeft = 50,
                borderRight = 50,
                minDistanceX = 50,
                minDistanceY = 50,
                optUnitX = Some("seconds30"),
                optUnitY = Some(10.0)
            )
            assertResult(expectedResult) {
                LineChartConfig(jsonString)
            }
        }
    }

}
