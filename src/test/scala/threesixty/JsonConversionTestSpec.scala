package threesixty

import threesixty.data.Data._
import threesixty.data.metadata._
import threesixty.data.DataJsonProtocol._

import org.scalatest.FunSpec

import spray.json._


class JsonConversionTestSpec extends FunSpec {
    // DATA
    describe("Identifier") {
        describe("""[ "a", "b", "c", "d" ]""") {
            it("should be convertible to Set[Identifier]") {
                assertResult(Set[Identifier]("a", "b", "c", "d")) {
                    ("""[ "a", "b", "c", "d" ]""").parseJson.convertTo[Set[Identifier]]
                }
            }
        }
        // ID_MAPPING
        describe("""{ "id1": "id1a", "id2": "id2b"}""") {
            it("should be convertible to Map[Identifier, Identifier]") {
                assertResult(Map[Identifier, Identifier]("id1" -> "id1a", "id2" -> "id2b")) {
                    ("""{ "id1": "id1a", "id2": "id2b"}""").parseJson.convertTo[Map[Identifier, Identifier]]
                }
            }
        }
    }

    // VisualizationConfig
    // ProcessingStrategy
    // ProcessingStep
    // ProcessingMethod


    describe("Conversion of JSON to Metadata values") {
        describe("Timeframe") {
            val json = """{"start": 1024, "end": 2048}"""
            it("is properly converted from JSON") {
                assertResult(Timeframe(new Timestamp(1024), new Timestamp(2048))) {
                    json.parseJson.convertTo[Timeframe]
                }
            }
            it("throws a DeserializationException when end is missing") {
                val invalidJson = """{"start": 1024}"""
                intercept[DeserializationException] {
                    invalidJson.parseJson.convertTo[Timeframe]
                }
            }
            it("throws a DeserializationException when start is missing") {
                val invalidJson = """{"start": 2048}"""
                intercept[DeserializationException] {
                    invalidJson.parseJson.convertTo[Timeframe]
                }
            }
            it("ignores additional entries") {
                val jsonAdd = """{"start": 1024, "end": 2048, "more": "stuff", "irrelevant": 123}"""
                assertResult(Timeframe(new Timestamp(1024), new Timestamp(2048))) {
                    jsonAdd.parseJson.convertTo[Timeframe]
                }
            }
            it("throws a DeserializationException when start is not a number") {
                val invalidJson = """{"start": "text", "end": 2048}"""
                intercept[DeserializationException] {
                    invalidJson.parseJson.convertTo[Timeframe]
                }
            }
            it("throws a DeserializationException when end is not a number") {
                val invalidJson = """{"start": 1024, "end": "text"}"""
                intercept[DeserializationException] {
                    invalidJson.parseJson.convertTo[Timeframe]
                }
            }
        }

        describe("Reliability") {
            val deviceJson = JsString("Device")
            val userJson = JsString("User")
            val unkownJson = JsString("Unknown")

            it("is properly converted from JSON") {
                assert(deviceJson.convertTo[Reliability.Value] == Reliability.Device)
                assert(userJson.convertTo[Reliability.Value] == Reliability.User)
                assert(unkownJson.convertTo[Reliability.Value] == Reliability.Unknown)
            }
            it("throws a DeserializationException when any other value is given") {
                val invalidJson = JsString("Banana")
                intercept[DeserializationException] {
                    invalidJson.convertTo[Reliability.Value]
                }
            }
        }


        describe("Resolution") {
            val highJson = JsString("High")
            val middleJson = JsString("Middle")
            val lowJson = JsString("Low")

            it("is properly converted from JSON") {
                assert(highJson.convertTo[Resolution.Value] == Resolution.High)
                assert(middleJson.convertTo[Resolution.Value] == Resolution.Middle)
                assert(lowJson.convertTo[Resolution.Value] == Resolution.Low)
            }
            it("throws a DeserializationException when any other value is given") {
                val invalidJson = JsString("Banana")
                intercept[DeserializationException] {
                    invalidJson.convertTo[Resolution.Value]
                }
            }
        }

        describe("Scaling") {
            val ordinalJson = JsString("Ordinal")
            val nominalJson = JsString("Nominal")

            it("is properly converted from JSON") {
                assert(ordinalJson.convertTo[Scaling.Value] == Scaling.Ordinal)
                assert(nominalJson.convertTo[Scaling.Value] == Scaling.Nominal)
            }
            it("throws a DeserializationException when any other value is given") {
                val invalidJson = JsString("Banana")
                intercept[DeserializationException] {
                    invalidJson.convertTo[Scaling.Value]
                }
            }
        }

        describe("ActivityType") {
            val json = """{"name": "Doing something"}"""
            it("is properly converted from JSON") {
                assertResult(ActivityType("Doing something")) {
                    json.parseJson.convertTo[ActivityType]
                }
            }
            it("throws a DeserializationException when name is missing") {
                val invalidJson = """{}"""
                intercept[DeserializationException] {
                    invalidJson.parseJson.convertTo[ActivityType]
                }
            }
            it("ignores additional entries") {
                val jsonAdd = """{"name": "Doing something", "more": "stuff", "irrelevant": 123}"""
                assertResult(ActivityType("Doing something")) {
                    json.parseJson.convertTo[ActivityType]
                }
            }
            it("throws a DeserializationException when name is not a string") {
                val invalidJson = """{"name": 1234}"""
                intercept[DeserializationException] {
                    invalidJson.parseJson.convertTo[ActivityType]
                }
            }
        }
    }

    describe("Conversion of JSON to IncompleteInputMetadata") {
        describe("a complete JSON description") {
            val jsonString = """{"timeframe": {
                    "start": 1024,
                    "end": 2048
                },
                "reliability": "Device",
                "resolution": "Middle",
                "scaling": "Nominal",
                "activityType": { "name": "Something"}
            }"""

            it("should end up as IncompleteInputMetadata with all the right values") {
                val metadata = jsonString.parseJson.convertTo[IncompleteInputMetadata]
                assert(metadata.timeframe.get == Timeframe(new Timestamp(1024), new Timestamp(2048)))
                assert(metadata.reliability.get == Reliability.Device)
                assert(metadata.resolution.get == Resolution.Middle)
                assert(metadata.scaling.get == Scaling.Nominal)
                assert(metadata.activityType.get == ActivityType("Something"))
            }
        }
    }

    // InputData

}
