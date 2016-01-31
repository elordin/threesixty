package threesixty.algorithms

import org.scalatest.FunSpec

import threesixty.algorithms.interpolation.{Aggregation, Accumulation, LinearInterpolation}
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Timestamp
import threesixty.data.tags._



class AggregationTestSpec extends FunSpec {

    describe("Aggregation") {

        describe("TimeAggregation") {
            val interpolator = Aggregation(2, Map("SomeID" -> "SomeID"))

            describe("from (0,0) to (3, 3)") {
                val sampleData = new ProcessedData("SomeID", List(
                    TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(3), 3.0, Set[Tag]())
                ))

                it("should be (0,0.5), (2,2.5)") {
                    val expectedResult = Set(ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(1), 0.5, Set[Tag](TimeAggregated)),
                        TaggedDataPoint(new Timestamp(3), 2.5, Set[Tag](TimeAggregated))
                    )))

                    assertResult(expectedResult) {
                        interpolator(sampleData)
                    }
                }
            }
        }

        describe("EnumAggregation") {
            val interpolator = Aggregation(-1, Map("SomeID" -> "SomeID"))

            describe("from (0,0) to (3, 3)") {
                val sampleData = new ProcessedData("SomeID", List(
                    TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(1), 0.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(2), 1.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(3), 0.0, Set[Tag]())
                ))

                it("should be (0,0), (0,1)") {
                    val expectedResult = Set(ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(0), 3, Set[Tag](new AggregationTag("0.0"))),
                        TaggedDataPoint(new Timestamp(0), 1, Set[Tag](new AggregationTag("1.0")))
                    )))

                    assertResult(expectedResult) {
                        interpolator(sampleData)
                    }
                }
            }
        }
    }

}
