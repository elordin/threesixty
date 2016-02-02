package threesixty.algorithms

import org.scalatest.FunSpec

import threesixty.algorithms.interpolation.{TimeSelection, Aggregation, Accumulation, LinearInterpolation}
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Timestamp
import threesixty.data.tags._



class TimeSelectionTestSpec extends FunSpec {

    describe("TimeSelection") {

        describe("TimeSelection1") {
            val aggregator = TimeSelection(new Timestamp(1), new Timestamp(2), Map("SomeID" -> "SomeID"))

            describe("from (0,0) to (3, 3)") {
                val sampleData = new ProcessedData("SomeID", List(
                    TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(3), 3.0, Set[Tag]())
                ))

                it("should be (0,0.5), (2,2.5)") {
                    val expectedResult = Set(ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag]()),
                        TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag]())
                    )))

                    assertResult(expectedResult) {
                        aggregator(sampleData)
                    }
                }
            }
        }
    }

}
