package threesixty.visualizer.visualizations

import org.scalatest.FunSpec
import threesixty.data.Data.Timestamp
import threesixty.visualizer.util.GreenColorScheme
import threesixty.visualizer.util.param.Border
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


class ScatterChartConversionTestSpec extends FunSpec {

    describe("A ScatterChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "title": "Title",
                "titleVerticalOffset": 50,
                "titleFontSize": 18,
                "xlabel": "X-Label",
                "ylabel": "Y-Label",
                "minDistanceX": 30,
                "minDistanceY": 40,
                "fontSize": 10,
                "fontFamily": "FontFamily",
                "xMin": -20.4,
                "xMax": 30,
                "yMin": -10,
                "yMax": 50.5,
                "xUnit": 25,
                "yUnit": 20.5,
                "radius": 4
            }"""


        it("should have all values set correctly") {
            val expectedResult = ScatterChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                _border = Some(Border(200,100,150,25)),
                _colorScheme = Some(GreenColorScheme),
                /*_title = Some("Title"),
                _titleVerticalOffset = Some(50),
                _titleFontSize = Some(18),
                _xLabel = Some("X-Label"),
                _yLabel = Some("Y-Label"),
                _minPxBetweenXGridPoints = Some(30),
                _minPxBetweenYGridPoints = Some(40),
                _lableFontSize = Some(10),
                _labelFontFamily = Some("FontFamily"),
                _xMin = Some(-20.4),
                _xMax = Some(30),
                _yMin = Some(-10),
                _yMax = Some(50.5),
                _xUnit = Some(25),
                _yUnit = Some(20.5),*/
                _radius = Some(4)
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
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "xlabel": "X-Label",
                "minDistanceX": 30,
                "fontFamily": "FontFamily",
                "xMax": 30,
                "yMin": -10,
                "yUnit": 20.5
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = ScatterChartConfig(jsonString)
            assert(convertedConfig.title == "")
            //assert(convertedConfig.titleVerticalOffset == 20)
            //assert(convertedConfig.titleFontSize == 20)
            //assert(convertedConfig.yLabel == "")
            //assert(convertedConfig.fontSize == 12)
            //assert(convertedConfig.minPxBetweenYGridPoints == 20)
            //assert(convertedConfig._xMin == None)
            //assert(convertedConfig._yMax == None)
            //assert(convertedConfig._xUnit == None)
            assert(convertedConfig.radius == 2)
        }

        it("should have all values set correctly") {
            val expectedResult = ScatterChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                _border = Some(Border(200,100,150,25)),
                _colorScheme = Some(GreenColorScheme)
                /*_xLabel = Some("X-Label"),
                _minPxBetweenXGridPoints = Some(30),
                _labelFontFamily = Some("FontFamily"),
                _xMax = Some(30),
                _yMin = Some(-10),
                _yUnit = Some(20.5)*/
            )
            assertResult(expectedResult) {
                ScatterChartConfig(jsonString)
            }
        }
    }
}
