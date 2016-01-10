package threesixty.algorithms

import org.scalatest.FunSpec

import threesixty.algorithms.interpolation.LinearInterpolation
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.tags.{Tag, Original, Interpolated}

import java.sql.Timestamp


class LinearInterpolationSpec extends FunSpec {

    describe("Linear interpolation") {
        describe("with resolution of 1") {
            val interpolator = LinearInterpolation(1, Map("SomeID" -> "SomeID"))
            describe("from (0,0) to (5, 5)") {
                val sampleData = new ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                        TaggedDataPoint(new Timestamp(5), 5.0, Set[Tag]())
                    ))
                it("should be (0,0), (1,1), (2,2), (3,3), (4,4), (5,5)") {


                    val expectedResult = Set(ProcessedData("SomeID", List(
                            TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag](Original)),
                            TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(3), 3.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(4), 4.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(5), 5.0, Set[Tag](Original))
                        )))

                    assertResult(expectedResult) {
                        interpolator(sampleData)
                    }
                }
            }

            describe("from (0,0) to (5,5) with all intermedirary points") {
                val sampleData = new ProcessedData("SomeID", List(
                    TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(3), 3.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(4), 4.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(5), 5.0, Set[Tag]())
                ))
                it ("should only append the Original tag") {
                    val expectedResult = Set(ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag](Original)),
                        TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag](Original)),
                        TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag](Original)),
                        TaggedDataPoint(new Timestamp(3), 3.0, Set[Tag](Original)),
                        TaggedDataPoint(new Timestamp(4), 4.0, Set[Tag](Original)),
                        TaggedDataPoint(new Timestamp(5), 5.0, Set[Tag](Original))
                    )))
                    assertResult(expectedResult) {
                        interpolator(sampleData)
                    }
                }
            }
        }

        describe("with resolution of 2") {
            val interpolator = LinearInterpolation(2, Map("SomeID" -> "SomeID"))
            describe("from (0,0) to (7,7) to (12, 12)") {
                val sampleData = ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                        TaggedDataPoint(new Timestamp(7), 7.0, Set[Tag]()),
                        TaggedDataPoint(new Timestamp(12), 12.0, Set[Tag]())
                    ))

                it("should be (0,0), (2,2), (4,4), (6,6), (7,7), (9,9), (11,11), (12,12)") {
                    val expectedResult = Set(ProcessedData("SomeID", List(
                            TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag](Original)),
                            TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(4), 4.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(6), 6.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(7), 7.0, Set[Tag](Original)),
                            TaggedDataPoint(new Timestamp(9), 9.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(11), 11.0, Set[Tag](Interpolated)),
                            TaggedDataPoint(new Timestamp(12), 12.0, Set[Tag](Original))
                        )))

                    assertResult(expectedResult) {
                        interpolator(sampleData)
                    }
                }
            }
        }
    }

}
