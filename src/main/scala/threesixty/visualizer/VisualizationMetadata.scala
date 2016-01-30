package threesixty.visualizer

/**
  * @author Thomas Engel
  */
case class VisualizationMetadata(val requirementList: List[DataRequirement],
                                 val unlimitedData: Boolean = false) {

    require(requirementList != null && requirementList.size > 0, "There must be at least one data required")

    def numberOfInputs(): Int = unlimitedData match {
        case true => Int.MaxValue
        case false => requirementList.size
    }
}

