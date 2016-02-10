package threesixty.visualizer.visualizations

import org.scalatest.FunSpec
import threesixty.data.Data.Timestamp
import threesixty.visualizer.util.GreenColorScheme
import threesixty.visualizer.util.param.{OptValueAxisParam, OptTimeAxisParam, OptTitleParam, OptBorder}
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
                "title": {"title": "Title", "position": "bottom", "verticalOffset": 15, "horizontalOffset": 25, "size": 22, "fontFamily": "fontFamily", "alignment": "end"},
                "xAxis": {"label": "X-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "min": -20.4, "max": 30, "minDistance": 30, "unit": 25, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showGrid": true, "showLabels": false, "arrowSize": 5, "arrowFilled": true},
                "yAxis": {"label": "Y-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "min": -10, "max": 50.5, "minDistance": 40, "unit": 20.5, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showGrid": false, "showLabels": true, "arrowSize": 7, "arrowFilled": false},
                "radius": 4
            }"""


        it("should have all values set correctly") {
            val expectedResult = ScatterChartConfig(
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
                _xAxis = Some(OptValueAxisParam(
                    label = Some("X-Label"),
                    labelSize = Some(10),
                    labelFontFamily = Some("fontFamily"),
                    min = Some(-20.4),
                    max = Some(30),
                    minPxBetweenGridPoints = Some(30),
                    unit = Some(25),
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
                "title": {"position": "bottom",  "horizontalOffset": 25, "size": 22, "fontFamily": "fontFamily", "alignment": "end"},
                "xAxis": {"labelSize": 10, "labelFontFamily": "fontFamily", "min": -20.4, "max": 30, "minDistance": 30, "unit": 25, "unitLabelFontFamily": "fontFamily", "showLabels": false, "arrowSize": 5, "arrowFilled": true},
                "yAxis": {"label": "Y-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "max": 50.5, "minDistance": 40, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showGrid": false, "showLabels": true, "arrowFilled": false}
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = ScatterChartConfig(jsonString)
            assert(convertedConfig.title.title == "")
            assert(convertedConfig.title.verticalOffset == 20)
            assert(convertedConfig.xAxis.label == "")
            assert(convertedConfig.xAxis.unitLabelSize == 12)
            assert(convertedConfig.xAxis.showGrid == true)
            assert(convertedConfig.yAxis.arrowSize == 10)
            assert(convertedConfig.radius == 2)
        }

        it("should have all values set correctly") {
            val expectedResult = ScatterChartConfig(
                ids = Seq("abc", "123"),
                height = 1024,
                width = 768,
                _border = Some(OptBorder(Some(200),Some(100),Some(150),Some(25))),
                _colorScheme = Some(GreenColorScheme),
                _title = Some(OptTitleParam(
                    position = Some("bottom"),
                    horizontalOffset = Some(25),
                    size = Some(22),
                    fontFamily = Some("fontFamily"),
                    alignment = Some("end"))),
                _xAxis = Some(OptValueAxisParam(
                    labelSize = Some(10),
                    labelFontFamily = Some("fontFamily"),
                    min = Some(-20.4),
                    max = Some(30),
                    minPxBetweenGridPoints = Some(30),
                    unit = Some(25),
                    unitLabelFontFamily = Some("fontFamily"),
                    showLabels = Some(false),
                    arrowSize = Some(5),
                    arrowFilled = Some(true))),
                _yAxis = Some(OptValueAxisParam(
                    label = Some("Y-Label"),
                    labelSize = Some(10),
                    labelFontFamily = Some("fontFamily"),
                    max = Some(50.5),
                    minPxBetweenGridPoints = Some(40),
                    unitLabelSize = Some(10),
                    unitLabelFontFamily = Some("fontFamily"),
                    showGrid = Some(false),
                    showLabels = Some(true),
                    arrowFilled = Some(false)))
            )
            assertResult(expectedResult) {
                ScatterChartConfig(jsonString)
            }
        }
    }
}
