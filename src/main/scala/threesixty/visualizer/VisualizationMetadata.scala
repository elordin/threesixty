package threesixty.visualizer

import threesixty.metadata.Resolution

/**
  * Created by Thomas on 30.12.2015.
  */
case class VisualizationMetadata(resolutionsc: List[Resolution.type]) {
    val resolutions: List[Resolution.type] = resolutionsc

    require(resolutions != null && resolutions.size > 0, "There must be at least one input required.")

    def numberOfInputs(): Int = resolutions.size
}
