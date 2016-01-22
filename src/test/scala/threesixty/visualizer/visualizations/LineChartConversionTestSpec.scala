package threesixty.visualizer.visualizations.LineChart

import threesixty.data.Data.Timestamp
import org.scalatest._


class LineChartConversionTestSpec extends FunSpec {

    describe("A LineChartConfig created from a JSON String with all arguments") {
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

        it("should have all values set correctly") {
            val expectedResult = LineChartConfig(
                ids = Set("abc", "123"),
                height = 1024,
                width = 768,
                optXMin = Some(new Timestamp(100000)),
                optXMax = Some(new Timestamp(200000)),
                optYMin = Some(10.0),
                optYMax = Some(123.456),
                xLabel = Some("X-Axis"),
                yLabel = Some("Y-Axis"),
                title = Some("Title"),
                borderTop = Some(100),
                borderBottom = Some(50),
                borderLeft = Some(50),
                borderRight = Some(50),
                minDistanceX = Some(50),
                minDistanceY = Some(50),
                optUnitX = Some("seconds30"),
                optUnitY = Some(10.0)
            )
            assertResult(expectedResult) {
                LineChartConfig(jsonString)
            }
        }
    }

    describe("A LineChartConfig created from a JSON String with arguments missing") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "optXMax": 200000,
                "optYMin": 10.0,
                "optYMax": 123.456,
                "yLabel": "Y-Axis",
                "title": "Title",
                "borderTop": 100,
                "borderBottom": 50,
                "borderRight": 50,
                "minDistanceX": 50,
                "minDistanceY": 50,
                "optUnitX": "seconds30"
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = LineChartConfig(jsonString)
            assert(convertedConfig.optXMin == None)
            assert(convertedConfig._xLabel == "")
            assert(convertedConfig._borderLeft == 50)
            assert(convertedConfig.optUnitY == None)
        }

        it("should have the correct values where they were given") {
            val convertedConfig = LineChartConfig(jsonString)
            assert(convertedConfig.ids == Set("abc", "123"))
            assert(convertedConfig.height == 1024)
            assert(convertedConfig.width == 768)
            assert(convertedConfig.optYMax == Some(123.456))
            assert(convertedConfig._title == "Title")
        }
    }

}
