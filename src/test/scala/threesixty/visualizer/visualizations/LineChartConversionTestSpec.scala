package threesixty.visualizer.visualizations.LineChart

import threesixty.data.Data.Timestamp
import org.scalatest._
import threesixty.visualizer.visualizations.lineChart.LineChartConfig


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
                "distanceTitle": 15,
                "minDistanceX": 50,
                "minDistanceY": 50,
                "optUnitX": "seconds30",
                "optUnitY": 10.0,
                "fontSizeTitle": 40,
                "fontSize": 20
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
                _xLabel = Some("X-Axis"),
                _yLabel = Some("Y-Axis"),
                _title = Some("Title"),
                _borderTop = Some(100),
                _borderBottom = Some(50),
                _borderLeft = Some(50),
                _borderRight = Some(50),
                _distanceTitle = Some(15),
                _minDistanceX = Some(50),
                _minDistanceY = Some(50),
                optUnitX = Some("seconds30"),
                optUnitY = Some(10.0),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20)
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
                "xMax": 200000,
                "yMin": 10.0,
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
            assert(convertedConfig.xLabel == "")
            assert(convertedConfig.borderLeft == 50)
            assert(convertedConfig.distanceTitle == 10)
            assert(convertedConfig.optUnitY == None)
            assert(convertedConfig.fontSizeTitle == 20)
            assert(convertedConfig.fontSize == 12)

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
