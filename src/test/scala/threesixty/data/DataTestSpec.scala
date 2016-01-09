package threesixty.data

import threesixty.data.tags._
import threesixty.data.metadata.InputMetadata

import org.scalatest._


class DataTestSpec extends FunSpec {

    describe("InputData") {
        describe("when created without data") {
            it("should throw an IllegalArgumentException") {
                intercept[IllegalArgumentException] {
                    val data = InputData("", Nil, InputMetadata(null, null, null, null, null))
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
            val inputData:InputData = InputData("", List(
                DataPoint(0, 0.0),
                DataPoint(5, 5.0)
            ),  InputMetadata(null, null, null, null, null))

            val processedData:ProcessedData = inputData

            it("should have the same data and InputOrigin Tags") {


                assertResult(processedData) {
                    ProcessedData("", List(
                        TaggedDataPoint(0, 0.0, Set(InputOrigin(inputData))),
                        TaggedDataPoint(5, 5.0, Set(InputOrigin(inputData)))
                    ))
                }
            }
        }

    }

}
