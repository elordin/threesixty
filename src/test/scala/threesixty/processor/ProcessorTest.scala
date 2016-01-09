package threesixty.processor

import org.scalatest.FunSpec

import threesixty.data.{ProcessedData, TaggedDataPoint, InputData}
import threesixty.data.Data.Identifier
import threesixty.data.tags.Tag
import threesixty.config.Config
import threesixty.algorithms.interpolation.LinearInterpolation


class ProcessorTestSpec extends FunSpec {

    describe("Processing strategy") {
        describe("that only does linear interpolation with resolution 1 and " +
                 "whose ID mapping does not override the original data") {
            describe("on the dataset (0,0), (5,5)") {
                val sampleData = ProcessedData("SomeId", List(
                    TaggedDataPoint(0, 0.0, Set[Tag]()),
                    TaggedDataPoint(5, 5.0, Set[Tag]())
                    )
                )

                val interpolator = LinearInterpolation(1, Map("SomeId" -> "SomeId_interpolated"))

                val processingStrategy = ProcessingStrategy(
                    ProcessingStep(interpolator, Set("SomeId"))
                )

                val config = new Config(Set("SomeId"))
                config.pushData(Set(sampleData))

                val expectedResult = interpolator(sampleData)

                processingStrategy.process(config)

                it("should insert the resulting dataset into the config") {
                    assert(config.datasets.contains("SomeId_interpolated"))
                    assert(Set(config.datasets("SomeId_interpolated")) == expectedResult)
                }

                it("should not remove the original data") {
                    assert(config.datasets.contains("SomeId"))
                    assert(config.datasets("SomeId") == sampleData)
                }
            }
        }
    }
}
