package threesixty.data.metadata

import threesixty.data.DataPoint

/**
  * @author Thomas Engel
  */
object Reliability extends Enumeration{
    val Device, User, Unknown = Value

    type Reliability = Reliability.Value

    def deduce(contextData: List[DataPoint]): Reliability = {
        Reliability.Unknown
    }
}
