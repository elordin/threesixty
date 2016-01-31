package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.Data.Identifier

/**
 *  Generic Configuration for a [[threesixty.visualizer.Visualization]].
 *  Acts as a factory for creating [[threesixty.visualizer.Visualization]]s.
 *
 *  @param ids Set of ids which are to be displayed in the visualization
 */
abstract class VisualizationConfig(
    ids: Set[Identifier],
    height: Int,
    width: Int,
    _title: Option[String] = None,
    _borderTop: Option[Int] = None,
    _borderBottom: Option[Int] = None,
    _borderLeft: Option[Int] = None,
    _borderRight: Option[Int] = None,
    _distanceTitle: Option[Int] = None,
    _fontSizeTitle: Option[Int] = None,
    _fontSize: Option[Int] = None
) extends Function1[DataPool, Visualization] {

    def _width: Int = width
    def _height: Int = height

    require(height > 0, "Value for height must be greater than 0.")
    require(width > 0, "Value for width must be greater than 0.")

    def title: String = _title.getOrElse("")

    def borderTopDefault: Int = 100
    def borderBottomDefault: Int = 50
    def borderLeftDefault: Int = 50
    def borderRightDefault: Int = 50
    def distanceTitleDefault: Int = 10

    def borderTop: Int = _borderTop.getOrElse(borderTopDefault)
    def borderBottom: Int = _borderBottom.getOrElse(borderBottomDefault)
    def borderLeft: Int = _borderLeft.getOrElse(borderLeftDefault)
    def borderRight: Int = _borderRight.getOrElse(borderRightDefault)
    def distanceTitle: Int = _distanceTitle.getOrElse(distanceTitleDefault)

    require(borderTop >= 0, "Negative value for borderTop is not allowed.")
    require(borderBottom >= 0, "Negative value for borderBottom is not allowed.")
    require(borderLeft >= 0, "Negative value for borderLeft is not allowed.")
    require(borderRight >= 0, "Negative value for borderRight is not allowed.")

    def fontSizeDefault: Int = 12
    def fontSizeTitleDefault: Int = 20

    def fontSize: Int = _fontSize.getOrElse(fontSizeDefault)
    def fontSizeTitle: Int = _fontSizeTitle.getOrElse(fontSizeTitleDefault)

    require(fontSize > 0, "Value for font size must be positive.")
    require(fontSizeTitle > 0, "Value for font size title must be positive.")

    // calculate the available height and width for the chart
    def chartWidth: Int = width - borderLeft - borderRight
    def chartHeight: Int = height - borderTop - borderBottom

    require(chartWidth > 0, "The available width for the chart must be greater than 0.")
    require(chartHeight > 0, "The available height for the chart must be greater than 0.")

    def chartOrigin: (Int, Int) = (0, 0)

    def viewBox: (Int, Int, Int, Int) = {
        val (x, y) = chartOrigin
        (x, y, width, height)
    }

    def lowerLimit = - chartOrigin._2 + height - borderBottom
    def upperLimit = - chartOrigin._2 + borderTop
    def leftLimit = - chartOrigin._1 + borderLeft
    def rightLimit = - chartOrigin._1 + width - borderRight

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
