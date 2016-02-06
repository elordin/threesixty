package threesixty.visualizer.visualizations.barChart

import org.scalatest.FunSpec
import threesixty.visualizer.util.{GreenColorScheme, DefaultColorScheme, Border}

class BarChartConversionTestSpec extends FunSpec {

    describe("A BarChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["123"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "title": "Title",
                "titleVerticalOffset": 50,
                "titleFontSize": 18,
                "xlabel": "X-Label",
                "ylabel": "Y-Label",
                "minDistanceY": 40,
                "fontSize": 10,
                "fontFamily": "FontFamily",
                "yMin": -10,
                "yMax": 50.5,
                "yUnit": 20.5,
                "widthBar": 20,
                "distanceBetweenBars": 50,
                "showValues": true
            }"""

        it("should have all values set correctly") {
            val expectedResult = BarChartConfig(
                ids = Seq("123"),
                height = 1024,
                width = 768,
                _border = Some(Border(200,100,150,25)),
                _colorScheme = Some(GreenColorScheme),
                _title = Some("Title"),
                _titleVerticalOffset = Some(50),
                _titleFontSize = Some(18),
                _xLabel = Some("X-Label"),
                _yLabel = Some("Y-Label"),
                _minPxBetweenYGridPoints = Some(40),
                _fontSize = Some(10),
                _fontFamily = Some("FontFamily"),
                _yMin = Some(-10),
                _yMax = Some(50.5),
                _yUnit = Some(20.5),
                _widthBar = Some(20),
                _distanceBetweenBars = Some(50),
                _showValues = Some(true)
            )
            assertResult(expectedResult) {
                BarChartConfig(jsonString)
            }
        }
    }

    describe("A BarChartConfig created from a JSON String with arguments missing") {
        val jsonString = """{
                "ids": ["123"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "title": "Title",
                "titleFontSize": 18,
                "ylabel": "Y-Label",
                "fontSize": 10,
                "fontFamily": "FontFamily",
                "yMax": 50.5,
                "widthBar": 20
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = BarChartConfig(jsonString)
            assert(convertedConfig.colorScheme == DefaultColorScheme)
            assert(convertedConfig.titleVerticalOffset == 20)
            assert(convertedConfig.xLabel == "")
            assert(convertedConfig.minPxBetweenYGridPoints == 20)
            assert(convertedConfig._yMin == None)
            assert(convertedConfig._yUnit == None)
            assert(convertedConfig._distanceBetweenBars == None)
            assert(convertedConfig.showValues == false)
        }

        it("should have all values set correctly") {
            val expectedResult = new BarChartConfig(
                ids = Seq("123"),
                height = 1024,
                width = 768,
                _border = Some(Border(200,100,150,25)),
                _title = Some("Title"),
                _titleFontSize = Some(18),
                _yLabel = Some("Y-Label"),
                _fontSize = Some(10),
                _fontFamily = Some("FontFamily"),
                _yMax = Some(50.5),
                _widthBar = Some(20)
            )
            assertResult(expectedResult) {
                BarChartConfig(jsonString)
            }
        }
    }
}
