package threesixty.data.metadata

import threesixty.data.InputData

/**
  * Created by Thomas on 30.12.2015.
  */
object Scaling extends Enumeration {
    val Ordinal, Nominal = Value

    type Scaling = Scaling.Value

    def deduce(contextData: InputData): Scaling = {
        Scaling.Nominal
    }
}
