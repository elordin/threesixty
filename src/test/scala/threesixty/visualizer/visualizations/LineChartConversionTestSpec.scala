package threesixty.visualizer.visualizations.LineChart

import threesixty.data.Data._
import org.scalatest._
import threesixty.data.DataJsonProtocol.TimestampJsonFormat
import threesixty.visualizer.util.param.{OptValueAxisParam, OptTimeAxisParam, OptTitleParam, OptBorder}
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
                "title": {"title": "Title", "position": "bottom",
                    "verticalOffset": 15, "horizontalOffset": 25, "size": 22, "fontFamily": "fontFamily", "alignment": "end"},
                "xAxis": {"label": "X-Label", "labelSize": 10, "labelFontFamily": "fontFamily",
                    "min": 1000, "max": 2000, "minDistance": 30, "unit": "1 month", "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showGrid": true, "showLabels": false, "arrowSize": 5, "arrowFilled": true},
                "yAxis": {"label": "Y-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "min": -10, "max": 50.5, "minDistance": 40, "unit": 20.5, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showGrid": false, "showLabels": true, "arrowSize": 7, "arrowFilled": false},
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
                _title = Some(OptTitleParam(
                    title = Some("Title"),
                    position = Some("bottom"),
                    verticalOffset = Some(15),
                    horizontalOffset = Some(25),
                    size = Some(22),
                    fontFamily = Some("fontFamily"),
                    alignment = Some("end"))),
                _xAxis = Some(OptTimeAxisParam(
                    label = Some("X-Label"),
                    labelSize = Some(10),
                    labelFontFamily = Some("fontFamily"),
                    min = Some(new Timestamp(1000)),
                    max = Some(new Timestamp(2000)),
                    minPxBetweenGridPoints = Some(30),
                    unit = Some("1 month"),
                    unitLabelSize = Some(10),
                    unitLabelFontFamily = Some("fontFamily"),
                    showGrid = Some(true),
                    showLabels = Some(false),
                    arrowSize = Some(5),
                    arrowFilled = Some(true))),
                _yAxis = Some(OptValueAxisParam(
                    label = Some("Y-Label"),
                    labelSize = Some(10),
                    labelFontFamily = Some("fontFamily"),
                    min = Some(-10),
                    max = Some(50.5),
                    minPxBetweenGridPoints = Some(40),
                    unit = Some(20.5),
                    unitLabelSize = Some(10),
                    unitLabelFontFamily = Some("fontFamily"),
                    showGrid = Some(false),
                    showLabels = Some(true),
                    arrowSize = Some(7),
                    arrowFilled = Some(false))),
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
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "title": {"title": "Title", "position": "bottom", "verticalOffset": 15, "size": 22, "alignment": "end"},
                "xAxis": {"label": "X-Label", "labelFontFamily": "fontFamily", "min": 1000, "max": 2000, "unit": "one month", "unitLabelSize": 10, "showGrid": true, "arrowSize": 5, "arrowFilled": true},
                "yAxis": {"label": "Y-Label", "labelSize": 10, "min": -10, "max": 50.5, "minDistance": 40, "unit": 20.5, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showLabels": true},
                "lineStrokeWidth": 3
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = LineChartConfig(jsonString)
            assert(convertedConfig.colorScheme == DefaultColorScheme)
            assert(convertedConfig.title.fontFamily == "Roboto, Segoe UI")
            assert(convertedConfig.title.horizontalOffset == 0)
            assert(convertedConfig.xAxis.labelSize == 12)
            assert(convertedConfig.xAxis.minPxBetweenGridPoints == 20)
            assert(convertedConfig.xAxis.unitLabelFontFamily == "Roboto, Segoe UI")
            assert(convertedConfig.xAxis.showLabels == true)
            assert(convertedConfig.yAxis.labelFontFamily == "Roboto, Segoe UI")
            assert(convertedConfig.yAxis.showGrid == true)
            assert(convertedConfig.yAxis.arrowSize == 10)
            assert(convertedConfig.yAxis.arrowFilled == false)
            assert(convertedConfig.radius == 2)
        }

        it("should have all values set correctly") {
            val expectedResult = LineChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(Some(200),Some(100),Some(150),Some(25))),
                _title = Some(OptTitleParam(
                    title = Some("Title"),
                    position = Some("bottom"),
                    verticalOffset = Some(15),
                    size = Some(22),
                    alignment = Some("end"))),
                _xAxis = Some(OptTimeAxisParam(
                    label = Some("X-Label"),
                    labelFontFamily = Some("fontFamily"),
                    min = Some(new Timestamp(1000)),
                    max = Some(new Timestamp(2000)),
                    unit = Some("one month"),
                    unitLabelSize = Some(10),
                    showGrid = Some(true),
                    arrowSize = Some(5),
                    arrowFilled = Some(true))),
                _yAxis = Some(OptValueAxisParam(
                    label = Some("Y-Label"),
                    labelSize = Some(10),
                    min = Some(-10),
                    max = Some(50.5),
                    minPxBetweenGridPoints = Some(40),
                    unit = Some(20.5),
                    unitLabelSize = Some(10),
                    unitLabelFontFamily = Some("fontFamily"),
                    showLabels = Some(true))),
                _lineStrokeWidth = Some(3)
            )
            assertResult(expectedResult) {
                LineChartConfig(jsonString)
            }
        }
    }

}
