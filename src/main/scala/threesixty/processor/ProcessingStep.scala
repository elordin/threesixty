package threesixty.processor

import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier
import threesixty.config.Config
import threesixty.visualizer.Visualization

import scala.collection.parallel._


sealed abstract class ProcessingMethod(idMapping: Map[Identifier, Identifier]) {
  def asProcessingStep: ProcessingStep = ProcessingStep(this, idMapping.keys.toSet)

  /*decution methods.
  return double[0;1] to indicate wether applying a ProcessingMethod
   on a certain InputData Set is suitable(1) or nonsense(0)*/
  def degreeOfFit(inputData: Set[InputData]): Double = {
    if (inputData.size == 0) {
      throw new IllegalArgumentException("empty inputdataSet in Deduction of ProcessingStrategy")
    }
    else if (inputData.size == 1) {
      computeDegreeOfFit(inputData.head)
    }
    else {
      //recursive call
      val head = inputData.head
      math.min(computeDegreeOfFit(head), degreeOfFit(inputData - head))
    }


}

  def degreeOfFit (inputData: Set[InputData], targetVisualization: Visualization): Double = {

    if (inputData.size == 0) {
      throw new IllegalArgumentException("empty inputdataSet in Deduction of ProcessingStrategy")
    }
    else if (inputData.size == 1) {
      computeDegreeOfFit(inputData.head, targetVisualization)
    }
    else {
      //recursive call
      val head = inputData.head
      math.min(computeDegreeOfFit(head,targetVisualization), degreeOfFit(inputData - head,targetVisualization))
    }
  }

//have to be overwritten in ProcessingMethods
def computeDegreeOfFit(inputData: InputData): Double
def computeDegreeOfFit(inputData: InputData, targetVisualization: Visualization) : Double


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


/**
  * Represents single step in the processing chain.
  *
  * Works on a subset of all data, namely all that data that is being subjected
  * to this particular processing method.
  *
  * Operations that run on each individual dataset without affecting the others
  * are run in parallel using scala.collection.parallel.ParSet.
  *
  * @author Thomas Weber
  *
  * @param method Method of processing for this step
  * @param IDs of a subset of all data that is to be processed in this step.
  */
case class ProcessingStep(val method: ProcessingMethod, val dataIDs: Set[Identifier]) {

  @throws[NoSuchElementException]("if ProcessedData for one of the ids could not be found")
  def run(datasetPool: Map[Identifier, ProcessedData]): Set[ProcessedData] = {
    method match {
      case m: SingleProcessingMethod => dataIDs.map(datasetPool(_)).par.flatMap(m(_)).seq
      case m: MultiProcessingMethod => m(dataIDs.map(datasetPool(_)))
    }
  }
}
