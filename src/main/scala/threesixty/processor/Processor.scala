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


/**
 *  Holds a list of available processing methods and some meta information.
 *
 *  Most notably knows and executes all conversions from JSON to [[threesixty.processor.ProcessingStep]].
 */
class Processor extends ProcessingMixins with UsageInfo {

    def usage = """ ... """ // TODO

    /**
     *  Converts a list of JSON encoded processing steps into a [[threesixty.processor.ProcessingStrategy]].
     *
     *  @param jsonString JSON representation of the list of ProcessingSteps
     */
    @throws[IllegalArgumentException]("if a parameter is missing")
    @throws[NoSuchElementException]("if a requested method doesn't exist")
    @throws[DeserializationException]("if the \"processor\" argument is not a list of ProcessingSteps")
    def toProcessingStrategy(jsonString: String): ProcessingStrategy = {
        val json = jsonString.parseJson

        val procSteps: Seq[ProcessingStep] = json match {
            case JsArray(steps) => steps.map({ s:JsValue => toProcessingStep(s.toString)} ).toSeq
            case _ => throw new DeserializationException(
                "Expected \"processor\" to be a list of processing steps.")
        }

        ProcessingStrategy(procSteps: _*)
    }

    /**
     *  Converts a single JSON object into a [[threesixty.processor.ProcessingStep]].
     *
     *  "name" defines what processing method to convert to
     *  "args" may define additional parameters for the [[threesixty.processor.ProcessingMethod]]
     *
     *  @param jsonString JSON representation of the ProcessingStep
     */
    @throws[IllegalArgumentException]("if a parameter is missing")
    @throws[NoSuchElementException]("if a requested method doesn't exist")
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
