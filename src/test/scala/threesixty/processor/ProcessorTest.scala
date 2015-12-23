package threesixty.processor

import org.scalatest.FunSpec

import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.tags.Tag
import threesixty.config.Config
import threesixty.algorithms.interpolation.LinearInterpolation


class ProcessorTestSpec extends FunSpec {

    describe("Processing strategy") {
        describe("that only does linear interpolation with resolution 1") {
            val processingStrategy = ProcessingStrategy(LinearInterpolation(1))
            describe("processing (0,0), (5,5)") {
                val sampleData = ProcessedData(List(
                    TaggedDataPoint(0, 0.0, Set[Tag]()),
                    TaggedDataPoint(5, 5.0, Set[Tag]())
                    )
                )
                it("should do proper linear interpolation.") {
                    assertResult(Set(LinearInterpolation(1)(sampleData))) {
                        processingStrategy(Set(sampleData), Config())
                    }
                }
            }
        }
    }
}
