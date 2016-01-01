package threesixty.data

import threesixty.data.metadata.InputMetadata

case class InputDataMetadata()


case class DataPoint(val timstamp:Int, val value:Double)


case class InputData(
        val id:String,
        val data: List[DataPoint],
        val metadata:InputMetadata) {

    require(data.length > 0, "Emtpy dataset not allowed.")

}
