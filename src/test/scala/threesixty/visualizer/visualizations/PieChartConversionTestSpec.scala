package threesixty.visualizer.visualizations

import java.sql.Timestamp

import org.scalatest.FunSpec
import threesixty.visualizer.util.{OptBorder, LegendPositionType, Border}
import threesixty.visualizer.util.LegendPositionType.LegendPosition
import threesixty.visualizer.visualizations.pieChart.PieChartConfig


class PieChartConversionTestSpec extends FunSpec {
    describe("A PieChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["abc"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "title": "Title",
                "titleVerticalOffset": 50,
                "titleFontSize": 18,
                "fontSize": 10,
                "fontFamily": "FontFamily",
                "legendPosition": "left",
                "legendHorizontalOffset": 30,
                "legendVerticalOffset": 5,
                "legendSymbolWidth": 25,
                "showSegmentLabels": false,
                "valueLabelRadiusPercent": 1.2,
                "segmentLabelLineColor": "#223344",
                "showValues": true,
                "angleStart": 0,
                "angleEnd": 180,
                "radius": 75,
                "innerRadiusPercent": 0.5
            }"""

        it("should have all values set correctly") {
            val expectedResult = PieChartConfig(
                ids = Seq("abc"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(Some(200),Some(100),Some(150),Some(25))),
                _colorScheme = Some("green"),
                _title = Some("Title"),
                _titleVerticalOffset = Some(50),
                _titleFontSize = Some(18),
                _fontSize = Some(10),
                _fontFamily = Some("FontFamily"),
                _legendPosition = Some("left"),
                _legendHorizontalOffset = Some(30),
                _legendVerticalOffset = Some(5),
                _legendSymbolWidth = Some(25),
                _showSegmentLabels = Some(false),
                _valueLabelRadiusPercent = Some(1.2),
                _segmentLabelLineColor = Some("#223344"),
                _showValues = Some(true),
                _angleStart = Some(0),
                _angleEnd = Some(180),
                _radius = Some(75),
                _innerRadiusPercent = Some(0.5)
            )
            assertResult(expectedResult) {
                PieChartConfig(jsonString)
            }
        }
    }

    describe("A PieChartConfig created from a JSON String with arguments missing") {
        val jsonString = """{
                "ids": ["abc"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "title": "Title",
                "titleVerticalOffset": 50,
                "fontSize": 10,
                "legendHorizontalOffset": 30,
                "legendSymbolWidth": 25,
                "showValues": true,
                "angleEnd": 180,
                "innerRadiusPercent": 0.5
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = PieChartConfig(jsonString)
            assert(convertedConfig.titleFontSize == 20)
            assert(convertedConfig.fontFamily == "Roboto, Segoe UI")
            assert(convertedConfig.legendPosition == Some(LegendPositionType.RIGHT))
            assert(convertedConfig.legendVerticalOffset == 20)
            assert(convertedConfig.showSegmentLabels == true)
            assert(convertedConfig._valueLabelRadiusPercent == None)
            assert(convertedConfig.segmentLabelLineColor == "#000000")
            assert(convertedConfig.angleStart == 90)
            assert(convertedConfig._radius == None)
        }

        it("should have all values set correctly") {
            val expectedResult = PieChartConfig(
                ids = Seq("abc"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(Some(200),Some(100),Some(150),Some(25))),
                _colorScheme = Some("green"),
                _title = Some("Title"),
                _titleVerticalOffset = Some(50),
                _fontSize = Some(10),
                _legendHorizontalOffset = Some(30),
                _legendSymbolWidth = Some(25),
                _showValues = Some(true),
                _angleEnd = Some(180),
                _innerRadiusPercent = Some(0.5)
            )
            assertResult(expectedResult) {
                PieChartConfig(jsonString)
            }
        }
    }
}
