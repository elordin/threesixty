package threesixty.processor

import org.scalatest.FunSpec

import threesixty.data.{ProcessedData, DataPoint, InputData, DataPool}
import threesixty.data.Data.{Timestamp, Identifier}
import threesixty.data.tags.Tag
import threesixty.data.metadata.{CompleteInputMetadata, Timeframe, Reliability, Resolution, Scaling, ActivityType}
import threesixty.persistence.DatabaseAdapter
import threesixty.algorithms.interpolation.LinearInterpolation


class ProcessorTestSpec extends FunSpec {

    describe("Processing strategy") {
        describe("that only does linear interpolation with resolution 1 and " +
                 "whose ID mapping does not override the original data") {
            describe("on the dataset (0,0), (5,5)") {
                val sampleData = InputData("SomeId", "", List(
                        DataPoint(new Timestamp(0), 0.0),
                        DataPoint(new Timestamp(5), 5.0)
                    ), CompleteInputMetadata(
                        Timeframe(new Timestamp(0), new Timestamp(1)),
                        Reliability.Unknown,
                        Resolution.Low,
                        Scaling.Ordinal,
                        ActivityType("something"),
                        2
                    )
                )

                val interpolator = LinearInterpolation(1, Map("SomeId" -> "SomeId_interpolated"))

                val processingStrategy = ProcessingStrategy(
                    ProcessingStep(interpolator, Set("SomeId"))
                )

                val pool = new DataPool(Set("SomeId"), new DatabaseAdapter {
                        def getDataset(id:Identifier):Either[String, InputData] = Right(sampleData)
                        def insertData(data:InputData):Either[String, Identifier] = ???
                        def appendData(data:InputData):Either[String, Identifier] = ???
                        def appendOrInsertData(data:InputData):Either[String, Identifier] = ???
                        def getMetadata(identifier: Identifier):Option[CompleteInputMetadata] = ???
                    })

                pool.pushData(Set[ProcessedData](sampleData))

                val expectedResult = interpolator(sampleData)

                processingStrategy.process(pool)

                it("should insert the resulting dataset into the pool") {
                    assert(pool.datasets.contains("SomeId_interpolated"))
                    assert(Set(pool.datasets("SomeId_interpolated")) == expectedResult)
                }

                it("should not remove the original data") {
                    assert(pool.datasets.contains("SomeId"))
                    assert(pool.datasets("SomeId") == (sampleData: ProcessedData))
                }
            }
        }
    }

}
