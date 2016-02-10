package threesixty.visualizer.visualizations.barChart

import org.scalatest.FunSpec
import threesixty.visualizer.util.param.{OptValueAxisParam, OptTitleParam, Border}
import threesixty.visualizer.util.{GreenColorScheme, DefaultColorScheme}
import threesixty.visualizer.util.param.OptBorder


class BarChartConversionTestSpec extends FunSpec {

    describe("A BarChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["123"],
                "height": 1024,
                "width": 768,
                "border": {"top": 200, "bottom": 100, "left": 150, "right": 25},
                "colorScheme": "green",
                "title": {"title": "Title", "position": "bottom", "verticalOffset": 15, "horizontalOffset": 25, "size": 22, "fontFamily": "fontFamily", "alignment": "end"},
                "xAxis": {"label": "X-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "arrowSize": 5, "arrowFilled": true},
                "yAxis": {"label": "Y-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "min": -10, "max": 50.5, "minDistance": 40, "unit": 20.5, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showGrid": false, "showLabels": true, "arrowSize": 7, "arrowFilled": false},
                "widthBar": 20,
                "distanceBetweenBars": 50,
                "showValues": true,
                "descriptionLabelSize": 10,
                "descriptionLabelFontFamily": "fontFamily",
                "valueLabelSize": 10,
                "valueLabelFontFamily": "fontFamily"
            }"""

        it("should have all values set correctly") {
            val expectedResult = BarChartConfig(
                ids = Seq("123"),
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
                _widthBar = Some(20),
                _distanceBetweenBars = Some(50),
                _showValues = Some(true),
                _descriptionLabelSize = Some(10),
                _descriptionLabelFontFamily = Some("fontFamily"),
                _valueLabelSize = Some(10),
                _valueLabelFontFamily = Some("fontFamily")
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
                "colorScheme": "green",
                "title": {"title": "Title", "position": "bottom", "verticalOffset": 15, "horizontalOffset": 25, "size": 22, "fontFamily": "fontFamily", "alignment": "end"},
                "xAxis": {"labelSize": 10, "labelFontFamily": "fontFamily", "arrowSize": 5},
                "yAxis": {"label": "Y-Label", "labelSize": 10, "labelFontFamily": "fontFamily", "min": -10, "max": 50.5, "minDistance": 40, "unit": 20.5, "unitLabelSize": 10, "unitLabelFontFamily": "fontFamily", "showLabels": true, "arrowFilled": false},
                "widthBar": 20,
                "descriptionLabelSize": 10,
                "valueLabelFontFamily": "fontFamily"
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = BarChartConfig(jsonString)
            assert(convertedConfig.xAxis.label == "")
            assert(convertedConfig.xAxis.arrowFilled == false)
            assert(convertedConfig.yAxis.showGrid == true)
            assert(convertedConfig.yAxis.arrowSize == 10)
            assert(convertedConfig._distanceBetweenBars == None)
            assert(convertedConfig.showValues == false)
            assert(convertedConfig.descriptionLabelFontFamily == "Roboto, Segoe UI")
            assert(convertedConfig.valueLabelSize == 12)
        }

        it("should have all values set correctly") {
            val expectedResult = BarChartConfig(
                ids = Seq("123"),
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
                    labelSize = Some(10),
                    labelFontFamily = Some("fontFamily"),
                    arrowSize = Some(5))),
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
                    showLabels = Some(true),
                    arrowFilled = Some(false))),
                _widthBar = Some(20),
                _descriptionLabelSize = Some(10),
                _valueLabelFontFamily = Some("fontFamily")
            )
            assertResult(expectedResult) {
                BarChartConfig(jsonString)
            }
        }
    }
}
