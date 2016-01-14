package threesixty.data

import threesixty.data.tags._
import threesixty.data.Data.Timestamp
import threesixty.data.metadata.{CompleteInputMetadata, Timeframe, Reliability, Resolution, Scaling, ActivityType}

import org.scalatest._


class DataTestSpec extends FunSpec {

    describe("InputData") {
        describe("when created without data") {
            it("should throw an IllegalArgumentException") {
                intercept[IllegalArgumentException] {
                    val data = InputData("", "", Nil, CompleteInputMetadata(
                            Timeframe(new Timestamp(0), new Timestamp(1)),
                            Reliability.Unknown,
                            Resolution.Low,
                            Scaling.Ordinal,
                            ActivityType("something")
                        )
                    )
                }
            }
        }
    }

    describe("ProcessedData") {
        describe("when created without data") {
            it("should throw an IllegalArgumentException") {
                intercept[IllegalArgumentException] {
                    val data = ProcessedData("", Nil)
                }
            }
        }
    }

    describe("The implicit conversion") {
        import threesixty.data.Implicits._
        describe("of InputData with data (0,0), (5,5) to ProcessedData") {
            val inputData:InputData = InputData("", "", List(
                DataPoint(new Timestamp(0), 0.0),
                DataPoint(new Timestamp(5), 5.0)
            ), CompleteInputMetadata(
                    Timeframe(new Timestamp(0), new Timestamp(1)),
                    Reliability.Unknown,
                    Resolution.Low,
                    Scaling.Ordinal,
                    ActivityType("something")
                )
            )

            val processedData:ProcessedData = inputData

            it("should have the same data and InputOrigin Tags") {


                assertResult(processedData) {
                    ProcessedData("", List(
                        TaggedDataPoint(new Timestamp(0), 0.0, Set(InputOrigin(inputData))),
                        TaggedDataPoint(new Timestamp(5), 5.0, Set(InputOrigin(inputData)))
                    ))
                }
            }
        }

    }

}
