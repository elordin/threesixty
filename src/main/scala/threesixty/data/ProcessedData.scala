package threesixty.data

import threesixty.data.tags.Tag


case class TaggedDataPoint(val timestamp:Int, val value:Double, val tags:Set[Tag]) {

}


case class ProcessedData(val data:List[TaggedDataPoint]) {

    require(data.length > 0, "Empty dataset not allowed.")

}
