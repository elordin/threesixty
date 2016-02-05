package threesixty.processor

import threesixty.ProcessingMethods.interpolation.LinearInterpolation

import org.scalatest.FunSpec

// import spray.json._
// import DefaultJsonProtocol._


class ProcessorJsonConversionTestSpec extends FunSpec {

    // ProcessingMethod
    describe("A ProcessingMethod like LinearInterpolation") {
        describe("when passed a JSON encoded configuration") {

            val jsonString = """{
                    "frequency": 2,
                    "idMapping": {
                        "data1": "data1i"
                    }
                }"""
            it("should be constructable from that JSON") {
                val expectedResult = LinearInterpolation(2, Map("data1" -> "data1i"))
                assertResult(expectedResult) {
                    LinearInterpolation.apply(jsonString)
                }
            }

            it("should be convertible into a ProcessingStep from that JSON") {
                val expectedResult = ProcessingStep(
                    LinearInterpolation(2, Map("data1" -> "data1i")),
                    Set("data1")
                )
                assertResult(expectedResult) {
                    LinearInterpolation.apply(jsonString).asProcessingStep
                }
            }
        }
    }

    describe("A processor with LinearInterpolation installed") {
        val processor = new Processor with LinearInterpolation.Mixin
        describe("when passed a LinearInterpolation config as JSON") {
            val jsonString = """{
                    "method": "linearinterpolation",
                    "args": {
                        "frequency": 2,
                        "idMapping": {
                            "data1": "data1i"
                        }
                    }
                }"""
            it("should convert it to a ProcessingStep") {
                val expectedResult = ProcessingStep(
                    LinearInterpolation(2, Map("data1" -> "data1i")),
                    Set("data1")
                )
                assertResult(expectedResult) {
                    processor.toProcessingStep(jsonString)
                }
            }
        }

        describe("when passed a JSON String representing the strategy of only doing LinearInterpolation") {
            val jsonString = """[{
                    "method": "linearinterpolation",
                    "args": {
                        "frequency": 2,
                        "idMapping": {
                            "data1": "data1i"
                        }
                    }
                }]"""
            it("should convert it to a ProcessingStrategy") {
                val expectedResult = ProcessingStrategy(ProcessingStep(
                    LinearInterpolation(2, Map("data1" -> "data1i")),
                    Set("data1")
                ))
                assertResult(expectedResult) {
                    processor.toProcessingStrategy(jsonString)
                }
            }
        }
    }
}
