package threesixty.processor

import threesixty.engine.UsageInfo


case class ProcessingMethodInfo(
    val name: String,
    val stepFromString: (String) => ProcessingStep,
    val usage: String
) extends UsageInfo


trait withProcessingInfos {
    def processingInfos: Map[String, ProcessingMethodInfo] = Map.empty
}


// TODO Extendability
class Processor extends withProcessingInfos with UsageInfo {

    def usage = """ ... """ // TODO

}
