package threesixty.visualizer.visualizations.general

import org.scalatest.FunSpec


class TimeAxisTestSpec extends FunSpec{

    describe("A TimeAxis") {
        val axis = AxisFactory.createAxis(AxisType.TimeAxis, AxisDimension.xAxis, 100, 10, 108, "label", Some(5))

        it("should calculate the correct number of grid points") {
            assert(axis.getNumberOfGridPoints == 11)
        }

        it("should calculate the correct unit") {
            assert(axis.getUnit == 10)
        }

        it("should get the correct minimum and maximum value displayed value") {
            assert(axis.getMinimumDisplayedValue == 10)
            assert(axis.getMaximumDisplayedValue == 110)
        }

        it("should correctly convert a value") {
            assert(axis.convert(60) == 50)
        }

        it("should get the correct grid labels") {
            val expectedLabels = List("0","10","20","30","40","50","60","70","80","90","100")
            assertResult(expectedLabels) {
                axis.getGridLabels
            }
        }

        it("should get the correct grid points and label") {
            val expectedLabels = List("0","10","20","30","40","50","60","70","80","90","100")
            val expectedValues = List(0,10,20,30,40,50,60,70,80,90,100)
            assertResult(expectedValues.zip(expectedLabels)) {
                axis.getGridPointsAndLabel
            }
        }

        it("should get the correct axis label") {
            assertResult("label (in ms)") {
                axis.getAxisLabel
            }
        }
    }
}
