package threesixty.metadata

import threesixty.data.InputData

/**
  * Created by Thomas on 30.12.2015.
  */
object Reliability extends Enumeration{
    val Device, User, Unknown = Value

    type Reliability = Reliability.type

    def deduce(contextData: InputData): Reliability = {
        throw new NotImplementedError
    }
}
