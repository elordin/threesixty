package threesixty.visualizer

import threesixty.metadata.Resolution

/**
  * Created by Thomas on 30.12.2015.
  */
case class VisualizationMetadata(val dataRequirement: List[VisualizationMatchingTuple]) {
    require(dataRequirement != null && dataRequirement.size > 0, "There must be at least one data required")

    def numberOfInputs(): Int = dataRequirement.size
}
