package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.metadata.Resolution.Resolution
import threesixty.data.metadata.Scaling.Scaling
import threesixty.goals.Goal
import threesixty.processor.{ProcessingStep, ProcessingMethod}

/**
 * This class contains the input data requirements for a visualization type.
 *
 * @param resolution the required resolution
 * @param scaling the required scaling
 * @param requiredProcessingMethods a list of required processing methods
 * @param excludedProcessingMethods a list of processing methods that can not be applied
 * @param requiredGoal the required goal
 *
 * @author Thomas Engel
 */
case class DataRequirement(val resolution: Option[Resolution] = None,
                           val scaling: Option[Scaling] = None,
                           val requiredProcessingMethods: Option[List[ProcessingMethod]] = None,
                           val excludedProcessingMethods: Option[List[ProcessingMethod]] = None,
                           val requiredGoal: Option[Goal] = None) {

    // def missingMethods(data: InputData, procStrat: ProcessingStrategy): Set[ProcessingMethod]

    /**
      *  Method to determine if the input data fulfills the requirement
      *
      *  @param data an input data
      *  @param procMeth the processing step
      *  @return true if the input data fulfills the requirement
      */
    def isMatchingData(data: InputData, procMeth: ProcessingStep): Boolean = {

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
                case Some(res) => res == data.metadata.resolution
                case None => true
            }

            // match scaling requirement
            val matchScaling = scaling match {
                case Some(scal) => scal == data.metadata.scaling
                case None => true
            }


            // check excluded processing methods
            val procNotExcluded = excludedProcessingMethods match {
                case Some(list) => list.contains(procMeth.method)
                case None => true
            }

            val goal = requiredGoal match {
                case Some(g) => true //TODO if goals shall be implemented. check for "Equality" of gaols
                case None => true
            }

            matchResolution && matchScaling && procNotExcluded && goal
        }
    }



}
