package threesixty.data

import org.scalatest._


class ProcessedDataTestSpec extends FlatSpec {

    "ProcessedData" should "throw an IllegalArgumentException when created without data" in {
        intercept[IllegalArgumentException] {
            val data = ProcessedData(Nil)
        }
    }

}
