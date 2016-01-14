package threesixty.data.metadata

import org.scalatest.FlatSpec

/**
  * @author Thomas Engel
  */
class ScalingTestSpec  extends FlatSpec {

    "A Scaling" should "deduce the default scaling for input data" in {
        assertResult(Scaling.Nominal) {Scaling.deduce(null)}
    }
}
