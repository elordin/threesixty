package threesixty.visualizer.visualizations.general

import org.scalatest.FunSpec
import threesixty.visualizer.visualizations.Segment

class SegmentTestSpec extends FunSpec {

    describe("A Segment object") {
        it("should calculate the correct circle point") {
            val expectedPoint = (1, -math.sqrt(3))
            val point = Segment.calculatePoint(60, 2)
            assert(math.abs(expectedPoint._1 - point._1) < 0.00001)
            assert(math.abs(expectedPoint._2 - point._2) < 0.00001)
        }

        it("should give the correct result if an angle is contained") {
            assert(Segment.isAngleContained(90, 180, 0))
            assert(!Segment.isAngleContained(-90, 180, 0))
        }
    }

    describe("A Segment with large arc flat and sweep flag") {
        val segment = new Segment(
            identifier = "id",
            description = "description",
            Set.empty,
            angleStart = 270,
            angleEnd = 45,
            radius = 1,
            innerRadius = 0.5,
            showValueLabel = true,
            valueRadius = 1.5,
            value = "value",
            fontSize = 10)

        it("should calculate the correct delta angle") {
            assert(segment.calculateDeltaAngles == -225)
        }

        it("should calculate the correct middle angle") {
            assert(segment.calculateAvgAngle == 270 - 225/2.0)
        }

        it("should get the correct sweep flag") {
            assert(segment.getSweepFlag == 1)
        }

        it("should calculate the correct path") {
            val expectedPath = "M -9.184850993605148E-17 0.5 L -1.8369701987210297E-16 1.0  A 1.0 1.0 0 0 1 -0.9238795325112867 -0.3826834323650899 A 1.0 1.0 0 0 1 0.7071067811865476 -0.7071067811865475 L 0.3535533905932738 -0.35355339059327373 A 0.5 0.5 0 0 0 -0.46193976625564337 -0.19134171618254495 A 0.5 0.5 0 0 0 -9.184850993605148E-17 0.5"
            assertResult(expectedPath) {
                segment.calculatePath
            }
        }

        it("should calculate the correct value anchor point") {
            val expectedPoint = (-1.38581929876693,-0.5740251485476349)
            assertResult(expectedPoint) {
                segment.calculateValueLabelAnchorPoint
            }
        }
    }

    describe("A Segment without large arc flag and without sweep flag") {
        val segment =  new Segment(
            identifier = "id",
            description = "description",
            Set.empty,
            angleStart = 45,
            angleEnd = 135,
            radius = 1,
            innerRadius = 0.5,
            showValueLabel = true,
            valueRadius = 1.5,
            value = "value",
            fontSize = 10)

        it("should calculate the correct delta angle") {
            assert(segment.calculateDeltaAngles == 90)
        }

        it("should calculate the correct middle angle") {
            assert(segment.calculateAvgAngle == 90)
        }

        it("should get the correct sweep flag") {
            assert(segment.getSweepFlag == 0)
        }

        it("should calculate the correct path") {
            val expectedPath = "M 0.3535533905932738 -0.35355339059327373 L 0.7071067811865476 -0.7071067811865475  A 1.0 1.0 0 0 0 6.123233995736766E-17 -1.0 A 1.0 1.0 0 0 0 -0.7071067811865475 -0.7071067811865476 L -0.35355339059327373 -0.3535533905932738 A 0.5 0.5 0 0 1 3.061616997868383E-17 -0.5 A 0.5 0.5 0 0 1 0.3535533905932738 -0.35355339059327373"
            assertResult(expectedPath) {
                segment.calculatePath
            }
        }

        it("should calculate the correct value anchor point") {
            val expectedPoint = (9.184850993605148E-17,-1.5)
            assertResult(expectedPoint) {
                segment.calculateValueLabelAnchorPoint
            }
        }
    }
}
