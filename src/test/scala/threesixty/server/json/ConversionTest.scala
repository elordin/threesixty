package threesixty.server.json

import org.scalatest._

// import spray.json.JsonParser.ParsingException

class JsonConversionSpec extends FlatSpec {
    // "Converting \" \" to JSON" should "throw a ParsingException" {
    //     intercept[ParsingException] {
    //         " ".toJson
    //     }
    // }
    "Head of an empty list" should "throw a NoSuchElementException" in {
        intercept[NoSuchElementException] {
            Nil.head
        }
    }
}
