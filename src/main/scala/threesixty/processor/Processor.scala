package threesixty.processor

import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.engine.UsageInfo
import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier
import threesixty.data.metadata.CompleteInputMetadata
import threesixty.visualizer.VisualizationConfig

import spray.json._
import DefaultJsonProtocol._


sealed trait ProcessingMethod {
    def idMapping: Map[Identifier, Identifier]
    def companion: ProcessingMethodCompanion
    def asProcessingStep: ProcessingStep = ProcessingStep(this, idMapping.keys.toSet)
}


/**
 *  ProcessingMethod that works only on one single dataset.
 *  It may however create datasets, and thus returns a Set of ProcessedData.
 *
 *  @author Thomas Weber
 *
 *  @param Single instance of ProcessedData it requires
 *  @return Set of ProcessedData, possibly including artificially created data
 */
trait SingleProcessingMethod
  extends ProcessingMethod
  with Function1[ProcessedData, Set[ProcessedData]]


/**
 *  ProcessingMethod that requires multiple datasets to process.
 *
 *  @author Thomas Weber
 *
 *  @param Set of ProcessedData that is going to process
 *  @return Set of ProcessedData, possibly including artificially created data
 */
trait MultiProcessingMethod
  extends ProcessingMethod
  with Function1[Set[ProcessedData], Set[ProcessedData]]


trait ProcessingMethodCompanion extends UsageInfo {
    def name: String
    def fromString: (String) => ProcessingStep
    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep


    /**
      * recursive call of computeDegreeOfFit
      * Note: minimum dominates. => a set of InputData gets value of least suitable InputData within that set.
      */
    def degreeOfFit(inputData: Set[InputData]): Double = {
        require(inputData.size > 0, "Empty inputdataSet in deduction of ProcessingStrategy not allowed.")
        inputData.map({ data => computeDegreeOfFit(data) }).min
    }


    /**
      * recursive call of computeDegreeOfFit for a given VisualizationType
      * Note: minimum dominates. => a set of InputData gets value of least suitable InputData within that set.
      */
    def degreeOfFit (inputData: Set[InputData], targetVisualization: VisualizationConfig): Double = {
        require(inputData.size > 0, "Empty inputdataSet in deduction of ProcessingStrategy not allowed.")
        inputData.map({ data => computeDegreeOfFit(data, targetVisualization) }).min
    }

    /**
      *  Deduction method. Has to be overwritten in every Processing Method by
      *  1) naming the relevant metadata
      *  2) linking of the  metadata information with numbers that indicate how applicable the method is for such data.
      *  3) ensuring that the maximum sum of all metadata scores is no greater than 1
      *
      *  @return Double in the interval [0;1] to indicate whether applying a ProcessingMethod on a certain InputData Set is suitable (1) or nonsense (0)
      */
    def computeDegreeOfFit(inputData: InputData): Double

    /**
      *  Deduction method.
      *      *
      *  Has to be overwritten in every Processing Method by
      *  1) naming all relevant Visualization types in a Pattern match
      *  2) linking the visualization types with numbers that indicate how applicable the method is for such a visualization.
      *  3) ensuring that the maximum sum of all metadata scores is no greater than 1
      *  4a) return the product of this sum and the result of DegreeOfFit(inputData)
      * 4b) if the given visualization requires a certain processing Method, do return 1 instead of the product described in 4a
      *
      *  @return Double in the interval [0;1] to indicate whether applying a ProcessingMethod on a certain InputData Set is suitable (1) or nonsense (0)
      */
    def computeDegreeOfFit(inputData: InputData, targetVisualization: VisualizationConfig) : Double
}


trait ProcessingMixins {
    def processingInfos: Map[String, ProcessingMethodCompanion] = Map.empty
}


/**
 *  Holds a list of available processing methods and some meta information.
 *
 *  Most notably knows and executes all conversions from JSON to [[threesixty.processor.ProcessingStep]]
 *  as well as deductions when the processing strategy is not defined.
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
        val method = json.fields.getOrElse("method",
                throw new IllegalArgumentException("parameter \"method\" missing for processing step")
            ).convertTo[String]

        val conversion: (String) => ProcessingStep =
            this.processingInfos.getOrElse(method,
                throw new NoSuchElementException(s"Unknown processing method $method")
            ).fromString

        val args: String = json.fields.getOrElse("args",
                throw new IllegalArgumentException(s"""parameter "args" missing for processing method $method""")
            ).toString

        conversion(args)
    }

    /**
     *  Deduces the best fitting ProcessingStrategy for a given Set of InputData.
     */
    def deduce(data: Set[InputData]): ProcessingStrategy = {
            ProcessingStrategy(processingInfos.values.par.map({
            info => (info, info.degreeOfFit(data))
        }).maxBy(_._2)._1.default(data.map({ data => (data.id, data.id) }).toMap))

    }

    /**
     *  Deduces the best fitting Processingstrategy for a given Set of InputData
     *  and a Visualization.
     */
    def deduce(data: Set[InputData], vizConf: VisualizationConfig): ProcessingStrategy = {
        ProcessingStrategy(processingInfos.values.par.map({
            info => (info, info.degreeOfFit(data, vizConf))
        }).maxBy(_._2)._1.default(data.map({ data => (data.id, data.id) }).toMap))
    }


    def deduce(metadata: (Identifier, CompleteInputMetadata)*): ProcessingStrategy = ???
    def deduce(vizConf: VisualizationConfig, metadata: (Identifier, CompleteInputMetadata)*): ProcessingStrategy = ???

}
