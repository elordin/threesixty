package threesixty.visualizer.visualizations

import org.scalatest.FunSpec
import threesixty.visualizer.util.param.{PositionType, OptLegendParam, OptTitleParam, OptBorder}
import threesixty.visualizer.util.{GreenColorScheme}
import threesixty.visualizer.visualizations.pieChart.PieChartConfig


class PieChartConversionTestSpec extends FunSpec {
    describe("A PieChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["abc"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "title": {"title": "Title", "position": "bottom", "verticalOffset": 15, "horizontalOffset": 25, "size": 22, "fontFamily": "fontFamily", "alignment": "end"},
                "legend": {"position": "left", "verticalOffset": 5, "horizontalOffset": 12, "symbolWidth": 15, "size": 10, "fontFamily": "fontFamily"},
                "showSegmentLabels": false,
                "segmentLabelSize": 10,
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
                _colorScheme = Some(GreenColorScheme),
                _title = Some(OptTitleParam(
                    title = Some("Title"),
                    position = Some("bottom"),
                    verticalOffset = Some(15),
                    horizontalOffset = Some(25),
                    size = Some(22),
                    fontFamily = Some("fontFamily"),
                    alignment = Some("end"))),
                _legend = Some(OptLegendParam(
                    position = Some("left"),
                    verticalOffset = Some(5),
                    horizontalOffset = Some(12),
                    symbolWidth = Some(15),
                    size = Some(10),
                    fontFamily = Some("fontFamily"))),
                _showSegmentLabels = Some(false),
                _segmentLabelSize = Some(10),
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
                "title": {"title": "Title", "verticalOffset": 15, "fontFamily": "fontFamily", "alignment": "end"},
                "legend": {"verticalOffset": 5, "horizontalOffset": 12, "size": 10, "fontFamily": "fontFamily"},
                "showSegmentLabels": false,
                "segmentLabelSize": 10,
                "segmentLabelLineColor": "#223344",
                "showValues": true,
                "angleEnd": 180,
                "radius": 75
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = PieChartConfig(jsonString)
            assert(convertedConfig.title.position == PositionType.TOP)
            assert(convertedConfig.title.horizontalOffset == 0)
            assert(convertedConfig.title.size == 20)
            assert(convertedConfig.legend.position == Some(PositionType.RIGHT))
            assert(convertedConfig.legend.symbolWidth == 10)
            assert(convertedConfig.valueLabelRadiusPercent == 1.1)
            assert(convertedConfig.angleStart == 90)
            assert(convertedConfig.innerRadiusPercent == 0.0)
        }

        it("should have all values set correctly") {
            val expectedResult = PieChartConfig(
                ids = Seq("abc"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(Some(200),Some(100),Some(150),Some(25))),
                _colorScheme = Some(GreenColorScheme),
                _title = Some(OptTitleParam(
                    title = Some("Title"),
                    verticalOffset = Some(15),
                    fontFamily = Some("fontFamily"),
                    alignment = Some("end"))),
                _legend = Some(OptLegendParam(
                    verticalOffset = Some(5),
                    horizontalOffset = Some(12),
                    size = Some(10),
                    fontFamily = Some("fontFamily"))),
                _showSegmentLabels = Some(false),
                _segmentLabelSize = Some(10),
                _segmentLabelLineColor = Some("#223344"),
                _showValues = Some(true),
                _angleEnd = Some(180),
                _radius = Some(75)
            )
            assertResult(expectedResult) {
                PieChartConfig(jsonString)
            }
        }
    }
}
