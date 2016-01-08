package threesixty.visualizer

import threesixty.data.InputData
import threesixty.metadata.{Scaling, Resolution}

/**
  * Created by Thomas on 01.01.2016.
  */
case class VisualizationMatchingTuple(val resolution: Resolution.type, val scaling: Scaling.type) {
    require(resolution != null, "Null value for resolution not allowed")
    require(scaling != null, "Null value for scaling not allowed")

    def isMatchingData(data: InputData): Boolean = {
        data.metadata.resolution == resolution && data.metadata.scaling == scaling
    }
}
