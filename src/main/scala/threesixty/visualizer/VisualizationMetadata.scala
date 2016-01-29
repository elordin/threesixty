package threesixty.visualizer


/**
  * This class represents the metadata for a visualization.
  * For each [[DataRequirement]] an input data that fulfills the requirements is required.
  *
  * @param dataRequirement a list of input data requirements
  * @param unlimitedData true iff the number of input data is not limited
  *
  * @author Thomas Engel
  */
case class VisualizationMetadata(val dataRequirement: List[DataRequirement],
                                 val unlimitedData: Boolean = false) {

    require(dataRequirement != null && dataRequirement.size > 0, "There must be at least one data required")

    /**
      * If the number of input data is unlimited [[Int.MaxValue]] is returned.
      *
      * @return the number of required input data
      */
    def numberOfInputs(): Int = unlimitedData match {
        case true => Int.MaxValue
        case false => dataRequirement.size
    }
}

