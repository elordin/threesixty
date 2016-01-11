package threesixty.data

import threesixty.data.metadata.InputMetadata
import Data.ValueType


case class DataPoint(val timstamp:Int, val value: ValueType)

case class InputData(
        val id: String,
        val measurement: String, //heartrate, temperature etc
        val dataPoints: List[DataPoint],
        val metadata: InputMetadata) {


    require(dataPoints.length > 0, "Emtpy dataset not allowed.")

}
