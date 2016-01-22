package threesixty.processor

import threesixty.engine.UsageInfo

import spray.json._
import DefaultJsonProtocol._


case class ProcessingMethodInfo(
    val name: String,
    val conversion: (String) => ProcessingStep,
    val usage: String
) extends UsageInfo


trait ProcessingMixins {
    def processingInfos: Map[String, ProcessingMethodInfo] = Map.empty
}


class Processor extends ProcessingMixins with UsageInfo {

    def usage = """ ... """ // TODO


    def toProcessingStrategy(jsonString: String): ProcessingStrategy = {
        val json = jsonString.parseJson

        val procSteps = json match {
            case JsArray(steps) => steps.map({ s:JsValue => toProcessingStep(s.toString)} ).toSeq
            case _ => throw new DeserializationException(
                "Expected \"processor\" to be a list of processing steps.")
        }

        ProcessingStrategy(procSteps: _*)
    }


    def toProcessingStep(jsonString: String): ProcessingStep = {
        val json: JsObject = jsonString.parseJson.asJsObject
        val method = try {
            json.getFields("method")(0).convertTo[String]
        } catch {
            case e:IndexOutOfBoundsException =>
                throw new IllegalArgumentException("parameter \"method\" missing for processing step")
        }

        val conversion: (String) => ProcessingStep =
            this.processingInfos.getOrElse(method,
                throw new NoSuchElementException(s"Unknown processing method $method")
            ).conversion

        val args: String = try {
            json.getFields("args")(0).toString
        } catch {
            case e:IndexOutOfBoundsException =>
                throw new IllegalArgumentException(s"""parameter "args" missing for processing method $method""")
        }

        conversion(args)
    }

}
