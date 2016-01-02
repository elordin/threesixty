package threesixty.metadata

import threesixty.data.InputData

/**
  * Created by Thomas on 30.12.2015.
  */
object Scaling extends Enumeration {
    val Ordinal, Nominal = Value

    type Scaling = Scaling.type

    def deduce(contextData: InputData): Scaling = {
        throw new NotImplementedError
    }
}
