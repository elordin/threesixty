package threesixty.visualizer

import threesixty.data.{InputDataSkeleton, DataPool}
import threesixty.data.metadata.Resolution.Resolution
import threesixty.data.metadata.Scaling.Scaling
import threesixty.processor.{ProcessingMethodCompanion, ProcessingStrategy, ProcessingStep, ProcessingMethod}

/**
 * This class contains the input data requirements for a visualization type.
 *
 * @param resolution the required resolution
 * @param scaling the required scaling
 * @param requiredProcessingMethods a list of required processing methods
 * @param excludedProcessingMethods a list of processing methods that can not be applied
 *
 * @author Thomas Engel
 */

case class DataRequirement(
    val resolution: Option[Resolution] = None,
    val scaling: Option[Scaling] = None,
    val requiredProcessingMethods: Option[List[ProcessingMethodCompanion]] = None,
    val excludedProcessingMethods: Option[List[ProcessingMethodCompanion]] = None
) {

    // def missingMethods(data: InputData, procStrat: ProcessingStrategy): Set[ProcessingMethod]

    /**
      *  Method to determine if the input data fulfills the requirement
      *
      *  @param data an input data
      *  @param procMeth the processing step
      *  @return true if the input data fulfills the requirement
      */
    def isMatchingData(skeleton: InputDataSkeleton, procMeth: ProcessingStep): Boolean = {
        //TODO Use ProcessingStrategy here instead of ProcessingStep!!!

        val procIsDemanded = requiredProcessingMethods match {
            case Some(list) => list.contains(procMeth.method)
            case None => false
        }


        //breakoption | if Method is required, don't care about the rest.
        if (procIsDemanded){
            true
        }
        else {

            // match resolution requirement
            val matchResolution = resolution match {
                case Some(res) => res == skeleton.metadata.resolution
                case None => true
            }

            // match scaling requirement
            val matchScaling = scaling match {
                case Some(scal) => scal == skeleton.metadata.scaling
                case None => true
            }


            // check excluded processing methods
            val procNotExcluded = excludedProcessingMethods match {
                case Some(list) => list.contains(procMeth.method)
                case None => true
            }

            matchResolution && matchScaling && procNotExcluded
        }
    }

    /**
      * Returns ProcessingSteps, that are required but not yet part of the ProcessingStrategy */
    def missingMethods(strategy: ProcessingStrategy) : Option[List[ProcessingMethodCompanion]] = {
       if (strategy == null){
           requiredProcessingMethods
       }
        else {
        requiredProcessingMethods match {
           case Some(reqList) => Some(reqList.diff(strategy.steps))
           case None => None
         }
       }
    }



}
