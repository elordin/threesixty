package threesixty.data

trait InputDataMetadata

case class DataPoint(val timstamp:Int, val value:AnyVal)


case class InputData(
        val id:AnyVal,
        val data: List[DataPoint],
        val metadata:InputDataMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
