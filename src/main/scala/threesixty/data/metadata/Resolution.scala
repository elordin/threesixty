package threesixty.data.metadata

import threesixty.data.InputData

/**
  * @author Thomas Engel
  */
object Resolution extends Enumeration{
    val High, Middle, Low = Value

    // TODO: Set proper values for boundaries
    // TODO: Write Tests
    val boundaryLow = 1
    val boundaryHigh = 2

    type Resolution = Resolution.Value

    def deduce(contextData: InputData): Resolution = {
        val avg = (1.0 * (contextData.dataPoints.last.timestamp.getTime - contextData.dataPoints.head.timestamp.getTime)) / contextData.dataPoints.size
        val erg = 1.0 / avg

        if (erg < boundaryLow) {
            Resolution.Low
        } else if (erg > boundaryHigh) {
            Resolution.High
        } else {
            Resolution.Middle
        }
    }
}
