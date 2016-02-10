package threesixty.visualizer.visualizations.barChart

import org.scalatest.FunSpec
import threesixty.visualizer.visualizations.BarElement

class BarElementTestSpec extends FunSpec {
    describe("A created BarElement with positive height") {
        val barElement = new BarElement(
            identifier = "id",
            xLeft = 5,
            width = 10,
            height = 50,
            description = "blah",
            classes = Set.empty,
            showValues = true,
            value = "val",
            valueLabelSize = 10)

        it("should calculate the correct path") {
            val expectedPath = "M 5.0 0 L 5.0 -50.0 L 15.0 -50.0 L 15.0 0 L 5.0 0"
            assertResult(expectedPath) {
                barElement.calculateBarPath
            }
        }

        it("should calculate the correct value anchor point") {
            val expectedPoint = (10.0, -60.0)
            assertResult(expectedPoint) {
                barElement.calculateValueAnchorPoint
            }
        }

        it("should calculate the correct description anchor point") {
            val expectedPoint = (10, 17)
            assertResult(expectedPoint) {
                barElement.calculateDescriptionAnchorPoint
            }
        }
    }

    describe("A created BarElement with negative height") {
        val barElement = new BarElement(identifier = "id",
            xLeft = 5,
            width = 10,
            height = -50,
            description = "blah",
            classes = Set.empty,
            showValues = true,
            value = "val",
            valueLabelSize = 1)

        it("should calculate the correct path") {
            val expectedPath = "M 5.0 0 L 5.0 50.0 L 15.0 50.0 L 15.0 0 L 5.0 0"
            assertResult(expectedPath) {
                barElement.calculateBarPath
            }
        }

        it("should calculate the correct value anchor point") {
            val expectedPoint = (10, 56)
            assertResult(expectedPoint) {
                barElement.calculateValueAnchorPoint
            }
        }

        it("should calculate the correct description anchor point") {
            val expectedPoint = (10, -10)
            assertResult(expectedPoint) {
                barElement.calculateDescriptionAnchorPoint
            }
        }
    }
}
