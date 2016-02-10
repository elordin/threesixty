package threesixty.visualizer.visualizations.LineChart

import threesixty.data.Data._
import org.scalatest._
import threesixty.data.DataJsonProtocol.TimestampJsonFormat
import threesixty.visualizer.util.{DefaultColorScheme, GreenColorScheme}
import threesixty.visualizer.util.param.OptBorder
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


class LineChartConversionTestSpec extends FunSpec {

    describe("A LineChartConfig created from a JSON String with all arguments") {
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
                "xMin": 1000,
                "xMax": 2000,
                "yMin": -10,
                "yMax": 50.5,
                "xUnit": "one month",
                "yUnit": 20.5,
                "radius": 4,
                "lineStrokeWidth": 3
            }"""

        it("should have all values set correctly") {
            val expectedResult = LineChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(Some(200),Some(100),Some(150),Some(25))),
                _colorScheme = Some(GreenColorScheme),
                /*_title = Some("Title"),
                _titleVerticalOffset = Some(50),
                _titleFontSize = Some(18),
                _xLabel = Some("X-Label"),
                _yLabel = Some("Y-Label"),
                _minPxBetweenXGridPoints = Some(30),
                _minPxBetweenYGridPoints = Some(40),
                _labelFontSize = Some(10),
                _labelFontFamily = Some("FontFamily"),
                _xMin = Some(new Timestamp(1000)),
                _xMax = Some(new Timestamp(2000)),
                _yMin = Some(-10),
                _yMax = Some(50.5),
                _xUnit = Some("one month"),
                _yUnit = Some(20.5),*/
                _radius = Some(4),
                _lineStrokeWidth = Some(3)
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
                "border": {"top": 125, "right": 75}
                "title": "Title",
                "titleVerticalOffset": 50,
                "ylabel": "Y-Label",
                "minDistanceX": 30,
                "fontSize": 10,
                "fontFamily": "FontFamily",
                "xMin": 1000,
                "yMax": 50.5,
                "xUnit": "one month",
                "radius": 4,
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = LineChartConfig(jsonString)
            assert(convertedConfig.border.bottom == 50)
            assert(convertedConfig.border.left == 50)
            assert(convertedConfig.border.right == 50)
            assert(convertedConfig.colorScheme == DefaultColorScheme)
            //assert(convertedConfig.titleFontSize == 20)
            // assert(convertedConfig.xLabel == "")
            // assert(convertedConfig.minPxBetweenXGridPoints == 20)
            //assert(convertedConfig._xMax == None)
            //assert(convertedConfig._yMin == None)
            //assert(convertedConfig._yUnit == None)
            assert(convertedConfig.lineStrokeWidth == 2)
        }

        it("should have all values set correctly") {
            val expectedResult = LineChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(top = Some(125), right = Some(75))),
                /*_title = Some("Title"),
                _titleVerticalOffset = Some(50),
                _yLabel = Some("Y-Label"),
                _minPxBetweenYGridPoints = Some(40),
                _labelFontSize = Some(10),
                _labelFontFamily = Some("FontFamily"),
                _xMin = Some(new Timestamp(1000)),
                _yMax = Some(50.5),
                _xUnit = Some("one month"),*/
                _radius = Some(4)
            )
            assertResult(expectedResult) {
                LineChartConfig(jsonString)
            }
        }
    }

}
