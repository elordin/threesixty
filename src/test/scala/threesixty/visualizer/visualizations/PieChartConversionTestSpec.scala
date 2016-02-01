package threesixty.visualizer.visualizations

import org.scalatest.FunSpec
import threesixty.visualizer.visualizations.pieChart.PieChartConfig


class PieChartConversionTestSpec extends FunSpec {
    describe("A PieChartConfig created from a JSON String with all arguments") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "title": "Title",
                "borderTop": 100,
                "borderBottom": 50,
                "borderLeft": 50,
                "borderRight": 50,
                "distanceTitle": 15,
                "angleStart": 0,
                "angleEnd": 180,
                "radius": 120,
                "innerRadiusPercent": 0.5,
                "showValues": false,
                "fontSizeTitle": 40,
                "fontSize": 20,
                "widthLegendSymbol": 20,
                "distanceLegend": 50
            }"""

        it("should have all values set correctly") {
            val expectedResult = PieChartConfig(
                ids = Set("abc", "123"),
                height = 1024,
                width = 768,
                _title = Some("Title"),
                _borderTop = Some(100),
                _borderBottom = Some(50),
                _borderLeft = Some(50),
                _borderRight = Some(50),
                _distanceTitle = Some(15),
                _angleStart = Some(0),
                _angleEnd = Some(180),
                _radius = Some(120),
                _innerRadiusPercent = Some(0.5),
                _showValues = Some(false),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20),
                _widthLegendSymbol = Some(20),
                _distanceLegend = Some(50)
            )
            assertResult(expectedResult) {
                PieChartConfig(jsonString)
            }
        }
    }

    describe("A PieChartConfig created from a JSON String with arguments missing") {
        val jsonString = """{
                "ids": ["abc", "123"],
                "height": 1024,
                "width": 768,
                "title": "Title",
                "borderTop": 100,
                "borderBottom": 50,
                "borderLeft": 50,
                "borderRight": 50,
                "distanceTitle": 15,
                "radius": 120,
                "showValues": false,
                "fontSizeTitle": 40,
                "fontSize": 20,
                "widthLegendSymbol": 20
            }"""

        it("should have the default values where none were given") {
            val convertedConfig = PieChartConfig(jsonString)
            assert(convertedConfig._angleStart == 90)
            assert(convertedConfig._angleEnd == -270)
            assert(convertedConfig._innerRadiusPercent == 0)
            assert(convertedConfig._distanceLegend == 20)
        }

        it("should have all values set correctly") {
            val expectedResult = PieChartConfig(
                ids = Set("abc", "123"),
                height = 1024,
                width = 768,
                _title = Some("Title"),
                _borderTop = Some(100),
                _borderBottom = Some(50),
                _borderLeft = Some(50),
                _borderRight = Some(50),
                _distanceTitle = Some(15),
                _radius = Some(120),
                _showValues = Some(false),
                _fontSizeTitle = Some(40),
                _fontSize = Some(20),
                _widthLegendSymbol = Some(20)
            )
            assertResult(expectedResult) {
                PieChartConfig(jsonString)
            }
        }
    }
}
