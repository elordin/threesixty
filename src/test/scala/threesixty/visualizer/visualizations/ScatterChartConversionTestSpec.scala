package threesixty.visualizer.visualizations

import org.scalatest.FunSpec
import threesixty.data.Data.Timestamp
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


class ScatterChartConversionTestSpec extends FunSpec {

    describe("A ScatterChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "optXMin": 20,
                "optXMax": 500,
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
                "optUnitX": 100.0,
                "optUnitY": 10.0,
                "fontSizeTitle": 40,
                "fontSize": 20
            }"""

        it("should have all values set correctly") {
            val expectedResult = new ScatterChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                optXMin = Some(20),
                optXMax = Some(500),
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
                optUnitX = Some(10),
                optUnitY = Some(10.0),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20)
            )
            assertResult(expectedResult) {
                ScatterChartConfig(jsonString)
            }
        }
    }

    describe("A ScatterChartConfig created from a JSON String with arguments missing") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "optXMax": 500,
                "optYMin": 10.0,
                "xLabel": "X-Axis",
                "yLabel": "Y-Axis",
                "title": "Title",
                "borderTop": 100,
                "borderBottom": 50,
                "borderLeft": 50,
                "distanceTitle": 15,
                "minDistanceY": 50,
                "optUnitY": 10.0,
                "fontSizeTitle": 40,
                "fontSize": 20
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = ScatterChartConfig(jsonString)
            assert(convertedConfig.optXMin == None)
            assert(convertedConfig.optYMax == None)
            assert(convertedConfig._borderRight == 50)
            assert(convertedConfig._minDistanceX == 20)
            assert(convertedConfig.optUnitX == None)
        }

        it("should have all values set correctly") {
            val expectedResult = new ScatterChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                optXMax = Some(500),
                optYMin = Some(10.0),
                _xLabel = Some("X-Axis"),
                _yLabel = Some("Y-Axis"),
                _title = Some("Title"),
                _borderTop = Some(100),
                _borderBottom = Some(50),
                _borderLeft = Some(50),
                _distanceTitle = Some(15),
                _minDistanceY = Some(50),
                optUnitY = Some(10.0),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20)
            )
            assertResult(expectedResult) {
                ScatterChartConfig(jsonString)
            }
        }
    }
}
