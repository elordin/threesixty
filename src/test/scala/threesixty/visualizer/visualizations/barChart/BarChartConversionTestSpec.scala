package threesixty.visualizer.visualizations.barChart

import org.scalatest.FunSpec

class BarChartConversionTestSpec extends FunSpec {

    describe("A BarChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
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
                "widthBar": 20,
                "distanceBetweenBars": 50,
                "showValues": true,
                "minDistanceY": 50,
                "optUnitY": 10.0,
                "fontSizeTitle": 40,
                "fontSize": 20
            }"""

        it("should have all values set correctly") {
            val expectedResult = BarChartConfig(
                ids = Set("abc", "123"),
                height = 1024,
                width = 768,
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
                _widthBar = Some(20),
                _distanceBetweenBars = Some(50),
                _showValues = Some(true),
                _minDistanceY = Some(50),
                optUnitY = Some(10.0),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20)
            )
            assertResult(expectedResult) {
                BarChartConfig(jsonString)
            }
        }
    }

    describe("A BarChartConfig created from a JSON String with arguments missing") {
        val jsonString =
            """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "optYMin": 10.0,
                "optYMax": 123.456,
                "xLabel": "X-Axis",
                "title": "Title",
                "borderTop": 100,
                "borderLeft": 50,
                "borderRight": 50,
                "widthBar": 20,
                "showValues": true,
                "optUnitY": 10.0,
                "fontSizeTitle": 40,
                "fontSize": 20
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = BarChartConfig(jsonString)
            assert(convertedConfig._yLabel == "")
            assert(convertedConfig._borderBottom == 50)
            assert(convertedConfig._distanceTitle == 10)
            assert(convertedConfig._distanceBetweenBars == None)
            assert(convertedConfig.minDistanceY == None)

        }

        it("should have all values set correctly") {
            val expectedResult = new BarChartConfig(
                ids = Set("abc", "123"),
                height = 1024,
                width = 768,
                optYMin = Some(10.0),
                optYMax = Some(123.456),
                _xLabel = Some("X-Axis"),
                _title = Some("Title"),
                _borderTop = Some(100),
                _borderLeft = Some(50),
                _borderRight = Some(50),
                _widthBar = Some(20),
                _showValues = Some(true),
                optUnitY = Some(10.0),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20)
            )
            assertResult(expectedResult) {
                BarChartConfig(jsonString)
            }
        }
    }
}
