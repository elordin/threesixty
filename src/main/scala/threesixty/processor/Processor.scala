package threesixty.processor

import threesixty.engine.UsageInfo
import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier
import threesixty.visualizer.VisualizationConfig

import spray.json._
import DefaultJsonProtocol._


sealed abstract class ProcessingMethod(idMapping: Map[Identifier, Identifier]) {
    def asProcessingStep: ProcessingStep = ProcessingStep(this, idMapping.keys.toSet)
}


/**
  * ProcessingMethod that works only on one single dataset.
  * It may however create datasets, and thus returns a Set of ProcessedData.
  *
  * @author Thomas Weber
  *
  * @param Single instance of ProcessedData it requires
  * @return Set of ProcessedData, possibly including artificially created data
  */
abstract class SingleProcessingMethod(idMapping: Map[Identifier, Identifier])
  extends ProcessingMethod(idMapping: Map[Identifier, Identifier])
  with Function1[ProcessedData, Set[ProcessedData]]


/**
  * ProcessingMethod that requires multiple datasets to process.
  *
  * @author Thomas Weber
  *
  * @param Set of ProcessedData that is going to process
  * @return Set of ProcessedData, possibly including artificially created data
  */
abstract class MultiProcessingMethod(idMapping: Map[Identifier, Identifier])
  extends ProcessingMethod(idMapping: Map[Identifier, Identifier])
  with Function1[Set[ProcessedData], Set[ProcessedData]]


trait ProcessingMethodCompanion extends UsageInfo {
    def name: String
    def fromString: (String) => ProcessingStep
    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep

    /**
     *  Decution methods
     *
     *  Note: minimum dominates => a set of InputData gets value of least suitable InputData within that set
     *
     *  @return Double in the interval [0;1] to indicate wether applying a ProcessingMethod on a certain InputData Set is suitable (1) or nonsense (0)
     */
    def degreeOfFit(inputData: Set[InputData]): Double = {
        require(inputData.size > 0, "Empty inputdataSet in deduction of ProcessingStrategy not allowed.")

        if (inputData.size == 1) {
            computeDegreeOfFit(inputData.head)
        } else {
            math.min(computeDegreeOfFit(inputData.head), degreeOfFit(inputData.tail))
        }
    }

    /**
     *  Decution method.
     *
     *  Note: minimum dominates. => a set of InputData gets value of least suitable InputData within that set.
     *
     *  @return Double in the interval [0;1] to indicate wether applying a ProcessingMethod on a certain InputData Set is suitable (1) or nonsense (0)
     */
    def degreeOfFit (inputData: Set[InputData], targetVisualization: VisualizationConfig): Double = {
        require(inputData.size > 0, "Empty inputdataSet in deduction of ProcessingStrategy not allowed.")

        if (inputData.size == 1) {
            computeDegreeOfFit(inputData.head, targetVisualization)
        } else {
            math.min(computeDegreeOfFit(inputData.head, targetVisualization),
                degreeOfFit(inputData.tail, targetVisualization))
        }
    }

    //have to be overwritten in ProcessingMethods
    def computeDegreeOfFit(inputData: InputData): Double
    def computeDegreeOfFit(inputData: InputData, targetVisualization: VisualizationConfig) : Double
}


trait ProcessingMixins {
    def processingInfos: Map[String, ProcessingMethodCompanion] = Map.empty
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
            ).fromString

        val args: String = try {
            json.getFields("args")(0).toString
        } catch {
            case e:IndexOutOfBoundsException =>
                throw new IllegalArgumentException(s"""parameter "args" missing for processing method $method""")
        }

        conversion(args)
    }

    def deduce(data: Set[InputData]): ProcessingStrategy = {
        ProcessingStrategy(processingInfos.values.par.map({
            info => (info, info.degreeOfFit(data))
        }).maxBy(_._2)._1.default(data.map({ data => (data.id, data.id) }).toMap))
    }

    def deduce(data: Set[InputData], vizConf: VisualizationConfig): ProcessingStrategy = {
        ProcessingStrategy(processingInfos.values.par.map({
            info => (info, info.degreeOfFit(data, vizConf))
        }).maxBy(_._2)._1.default(data.map({ data => (data.id, data.id) }).toMap))
    }

}
