package threesixty.metadata

import threesixty.data.InputData

/**
  * Created by Thomas on 30.12.2015.
  */
object Resolution extends Enumeration{
    val High, Middle, Low = Value

    type Resolution = Resolution.type

    def deduce(contextData: InputData): Resolution = {
        throw new NotImplementedError
    }
}
