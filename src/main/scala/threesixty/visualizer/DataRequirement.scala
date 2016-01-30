package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.metadata.Resolution.Resolution
import threesixty.data.metadata.Scaling.Scaling
import threesixty.goals.Goal
import threesixty.processor.{ProcessingStep, ProcessingMethod}

/**
  * @author Thomas Engel
  */
case class DataRequirement(val resolution: Option[Resolution] = None,
                           val scaling: Option[Scaling] = None,
                           val requiredProcessingMethods: Option[List[ProcessingMethod]] = None,
                           val excludedProcessingMethods: Option[List[ProcessingMethod]] = None,
                           val requiredGoal: Option[Goal] = None) {

    /**
      *  Method to determine if the input data fulfills the requirement
      *
      *  @param data an input data
      *  @param pool Pool of datasets
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
