package threesixty

import threesixty.data.Data.Identifier

import org.scalatest.FunSpec

import spray.json._
import DefaultJsonProtocol._


class JsonConversionTestSpec extends FunSpec {
    // DATA
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

    // VisualizationConfig
    // ProcessingStrategy
    // ProcessingStep
    // ProcessingMethod
}
