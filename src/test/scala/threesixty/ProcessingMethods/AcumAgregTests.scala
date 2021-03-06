package threesixty.ProcessingMethods

import org.scalatest.FunSpec
import threesixty.ProcessingMethods.Accumulation.Accumulation

import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Timestamp
import threesixty.data.tags.{Accumulated, Tag}



class AcumAgregSpec extends FunSpec {

    describe("Accumulation") {
        val interpolator = new Accumulation(Map("SomeID" -> "SomeID"))


        describe("easy case") {
            val interpolator = new Accumulation(Map("SomeID" -> "SomeID"))

            describe("from (0,0) to (3, 3)") {
                val sampleData = new ProcessedData("SomeID", List(
                    TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(2), 2.0, Set[Tag]()),
                    TaggedDataPoint(new Timestamp(3), 3.0, Set[Tag]())
                ))

                it("should be (0,0), (1,1), (2,3), (3,6)") {
                    val expectedResult = Set(ProcessedData("SomeID", List(
                        TaggedDataPoint(new Timestamp(0), 0.0, Set[Tag](Accumulated)),
                        TaggedDataPoint(new Timestamp(1), 1.0, Set[Tag](Accumulated)),
                        TaggedDataPoint(new Timestamp(2), 3.0, Set[Tag](Accumulated)),
                        TaggedDataPoint(new Timestamp(3), 6.0, Set[Tag](Accumulated))
                    )))

                    assertResult(expectedResult) {
                        interpolator(sampleData)
                    }
                }
            }
        }
    }

}
