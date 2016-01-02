package threesixty.data

import threesixty.data.metadata.CompleteInputMetadata

case class DataPoint(val timstamp:Int, val value:AnyVal)


case class InputData(
        val id:AnyVal,
        val data: List[DataPoint],
        val metadata: CompleteInputMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
