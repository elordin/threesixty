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
        val segment = new Segment("id", "description", 270, 45, 1, 0.5, 1.5, "value", Some(10))

        it("should calculate the correct delta angle") {
            assert(segment.calculateDeltaAngles == -225)
        }

        it("should calculate the correct middle angle") {
            assert(segment.calculateAvgAngle == 270 - 225/2.0)
        }

        it("should get the correct large arc flag") {
            assert(segment.getLargeArcFlag == 1)
        }

        it("should get the correct sweep flag") {
            assert(segment.getSweepFlag == 1)
        }

        it("should calculate the correct path") {
            val expectedPath = "M -9.184850993605148E-17 0.5 L -1.8369701987210297E-16 1.0  A 1.0 1.0 0 1 1 0.7071067811865476 -0.7071067811865475 L 0.3535533905932738 -0.35355339059327373 A 0.5 0.5 0 1 0 -9.184850993605148E-17 0.5"
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
        val segment = new Segment("id", "description", 45, 135, 1, 0.5, 1.5, "value", Some(10))

        it("should calculate the correct delta angle") {
            assert(segment.calculateDeltaAngles == 90)
        }

        it("should calculate the correct middle angle") {
            assert(segment.calculateAvgAngle == 90)
        }

        it("should get the correct large arc flag") {
            assert(segment.getLargeArcFlag == 0)
        }

        it("should get the correct sweep flag") {
            assert(segment.getSweepFlag == 0)
        }
    }
}
