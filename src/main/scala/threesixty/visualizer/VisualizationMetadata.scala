package threesixty.visualizer

import threesixty.data.metadata.Resolution

/**
  * Created by Thomas on 30.12.2015.
  */
/* Commented because purpose is unclear and causes compile errors
case class VisualizationMetadata(val dataRequirement: List[VisualizationMatchingTuple]) {
    require(dataRequirement != null && dataRequirement.size > 0, "There must be at least one data required")

    def numberOfInputs(): Int = dataRequirement.size
}
*/

case class VisualizationMetadata()
