package threesixty.visualizer

/**
  * @author Thomas Engel
  */
case class VisualizationMetadata(val dataRequirement: List[DataRequirement],
                                 val unlimitedData: Boolean = false) {

    require(dataRequirement != null && dataRequirement.size > 0, "There must be at least one data required")

    def numberOfInputs(): Int = unlimitedData match {
        case true => Int.MaxValue
        case false => dataRequirement.size
    }
}

