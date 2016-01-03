package threesixty.metadata

import org.scalatest.FlatSpec

/**
  * @author Thomas Engel
  */
class ReliabilityTestSpec extends FlatSpec {

    "A Reliability" should "deduce the default reliability for input data" in {
        assertResult(Reliability.Unknown) {Reliability.deduce(null)}
    }
}
