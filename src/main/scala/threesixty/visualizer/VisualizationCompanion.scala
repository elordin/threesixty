package threesixty.visualizer

import threesixty.data.{ InputData, DataPool }
import threesixty.engine.UsageInfo


/** Trait for companion objects to  [[threesixty.visualizer.Visualization]]. */
trait VisualizationCompanion extends UsageInfo {
    /** Verbose name of the visualization */
    def name: String

    /** Conversion from String to [[threesixty.visualizer.VisualizationConfig]]. */
    def fromString: (String) => VisualizationConfig


    val metadata: VisualizationMetadata

    /**
     *  Method to determine if a list of input data fulfills the requirements of the visualization
     *
     *  @param inputData a list of input data
     *  @param pool the data pool
     *  @return a maybe reordered list of input data that matches the visualization requirement
     */
    def isMatching(inputData: List[InputData], pool: DataPool): Option[List[InputData]] = {
        if(metadata.unlimitedData) {
            // Unlimited data that all have to match the same data requirement
            var matching = true
            val dataRequirement = metadata.dataRequirement.head
            for(i <- 0 until inputData.size) {
                matching = matching && dataRequirement.isMatchingData(inputData(i), pool)
            }
            val result = matching match {
                case true => Some(inputData)
                case false => None
            }
            result
        } else if(metadata.numberOfInputs() != inputData.size) {
            // Wrong number of data for that visualization
            None
        } else {
            // Determine if any order of input data can be matched to the visualization
            // Build matrix that determines if a specific input data can be matched to a specific data requirement
            val matchingMatrix = Array.ofDim[Boolean](inputData.size, metadata.dataRequirement.size)
            for (i <- 0 until inputData.size;
                 k <- 0 until metadata.dataRequirement.size) {
                    matchingMatrix(i)(k) = metadata.dataRequirement(k).isMatchingData(inputData(i), pool)
            }

            // Check all permutations of input data if any of them can be matched
            val permutations = List.range(0, inputData.size - 1).permutations.toList

            for(p <- 0 until permutations.size) {
                val perm = permutations(p)
                var matching = true
                // Look up in matching matrix
                var requirementIndex = 0
                for(i <- 0 until inputData.size) {
                    val dataIndex = perm(i)
                    matching = matching && matchingMatrix(dataIndex)(requirementIndex)
                    requirementIndex += 1
                }

                // Build matching order of input data
                if(matching) {
                    var result: List[InputData] = List()
                    for(i <- 1 to perm.size) {
                        result = inputData(perm.size - i) :: result
                    }
                    Some(result)
                }
            }

            None
        }
    }
}
