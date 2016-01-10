package threesixty.metadata

import threesixty.data.InputData

/**
  * @author Thomas Engel
  */
object Reliability extends Enumeration{
    val Device, User, Unknown = Value

    type Reliability = Reliability.Value

    def deduce(contextData: InputData): Reliability = {
        Reliability.Unknown
    }
}
