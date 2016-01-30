package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.Data.Identifier
import threesixty.processor.{ProcessingStep, ProcessingMethod}

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
    title: Option[String] = None,
    borderTop: Option[Int] = None,
    borderBottom: Option[Int] = None,
    borderLeft: Option[Int] = None,
    borderRight: Option[Int] = None,
    distanceTitle: Option[Int] = None,
    fontSizeTitle: Option[Int] = None,
    fontSize: Option[Int] = None
) extends Function1[DataPool, Visualization] {

    def _width: Int = width
    def _height: Int = height

    require(height > 0, "Value for height must be greater than 0.")
    require(width > 0, "Value for width must be greater than 0.")

    def _title: String = title.getOrElse("")

    def borderTopDefault: Int = 100
    def borderBottomDefault: Int = 50
    def borderLeftDefault: Int = 50
    def borderRightDefault: Int = 50
    def distanceTitleDefault: Int = 10

    def _borderTop: Int = borderTop.getOrElse(borderTopDefault)
    def _borderBottom: Int = borderBottom.getOrElse(borderBottomDefault)
    def _borderLeft: Int = borderLeft.getOrElse(borderLeftDefault)
    def _borderRight: Int = borderRight.getOrElse(borderRightDefault)
    def _distanceTitle: Int = distanceTitle.getOrElse(distanceTitleDefault)

    require(_borderTop >= 0, "Negative value for borderTop is not allowed.")
    require(_borderBottom >= 0, "Negative value for borderBottom is not allowed.")
    require(_borderLeft >= 0, "Negative value for borderLeft is not allowed.")
    require(_borderRight >= 0, "Negative value for borderRight is not allowed.")

    def fontSizeDefault: Int = 12
    def fontSizeTitleDefault: Int = 20

    def _fontSize: Int = fontSize.getOrElse(fontSizeDefault)
    def _fontSizeTitle: Int = fontSizeTitle.getOrElse(fontSizeTitleDefault)

    require(_fontSize > 0, "Value for font size must be positive.")
    require(_fontSizeTitle > 0, "Value for font size title must be positive.")

    // calculate the available height and width for the chart
    def heightChart: Int = height - _borderTop - _borderBottom
    def widthChart: Int = width - _borderLeft - _borderRight

    require(heightChart > 0, "The available height for the chart must be greater than 0.")
    require(widthChart > 0, "The available width for the chart must be greater than 0.")

    def calculateOrigin: (Double, Double) = (0.0, 0.0)

    def calculateViewBox: (Double, Double, Int, Int) = {
        val (x, y) = calculateOrigin

        (-x, -y, width, height)
    }

    def lowerLimit = - calculateOrigin._2 + height - _borderBottom
    def upperLimit = - calculateOrigin._2 + _borderTop
    def leftLimit = - calculateOrigin._1 + _borderLeft
    def rightLimit = - calculateOrigin._1 + width - _borderRight

    val metadata: VisualizationMetadata




    /**
     *  Method to determine if a list of input data fulfills the requirements of the visualization
     *
     *  @param inputData a list of input data
     *  @param config the configuration
     *  @return a maybe reordered list of input data that matches the visualization requirement.
      *          It does not return a Boolean!
     */
    def isMatching(inputData: List[InputData], procMeth: ProcessingStep): Option[List[InputData]] = {

        if(metadata.unlimitedData) {
           isMatching_unlimited(inputData, procMeth)

        } else if(metadata.numberOfInputs() != inputData.size) {
            // Wrong number of data for that visualization
            None
        } else {
            // Determine if any order of input data can be matched to the visualization
            isMatching_limited(inputData,procMeth)
        }
    }

    private def isMatching_unlimited(inputData: List[InputData], procMeth: ProcessingStep) : Option[List[InputData]] = {
        // Unlimited data that all have to match the same data requirement
        var matching = true
        val dataRequirement = metadata.requirementList.head
        for(i <- 0 until inputData.size) {
            matching = matching && dataRequirement.isMatchingData(inputData(i), procMeth)
        }
        val result = matching match {
            case true => Some(inputData)
            case false => None
        }
        result
    }

    private def isMatching_limited(inputData: List[InputData], procMeth : ProcessingStep): Option[List[InputData]] = {

        // Build matrix that determines if a specific input data can be matched to a specific data requirement
        val matchingMatrix = buildMatchingMatrix(inputData,procMeth)

        // Check all permutations of input data if any of them can be matched
        val permutations = List.range(0, inputData.size ).permutations.toList
        for(p <- 0 until permutations.size) {
             val temp = checkPermutations(permutations, inputData, matchingMatrix,p)
               temp match {
                   case Some(list) => return Some(list) //force exit and return this value
                   case None => {} //skip
               }
        }
        None //is only reached if return-statement is never met

    }


    private def buildMatchingMatrix(inputData: List[InputData], procMeth: ProcessingStep) = {
         val matchingMatrix = Array.ofDim[Boolean](inputData.size, metadata.requirementList.size)
        for (i <- 0 until inputData.size;
             k <- 0 until metadata.requirementList.size) {
            matchingMatrix(i)(k) = metadata.requirementList(k).isMatchingData(inputData(i), procMeth)
        }

        matchingMatrix
    }

    private  def checkPermutations( permutations : List[List[Int]], inputData: List[InputData],
                                    matchingMatrix: Array[Array[Boolean]], p : Int): Option[List[InputData]] ={

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
                result = inputData(perm.size - i ) :: result
            }
            return Some(result) //force break and return this value
        }
        None
    }

}
