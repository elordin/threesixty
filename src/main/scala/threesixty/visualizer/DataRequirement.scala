package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.metadata.Resolution.Resolution
import threesixty.data.metadata.Scaling.Scaling
import threesixty.goals.Goal
import threesixty.processor.ProcessingMethod

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
      *  @return true iff the input data fulfills the requirement
      */
    def isMatchingData(data: InputData, pool: DataPool): Boolean = {
        // match resolution requirement
        val matchResolution = resolution match {
            case Some(res) => false // res == data.metadata.resolution
            case None => true
        }

        // match scaling requirement
        val matchScaling = scaling match {
            case Some(sc) => false // sc == data.metadata.scaling
            case None => true
        }

        // check excluded processing methods
        // TODO

        // check goal
        // TODO

        matchResolution && matchScaling
    }
}
