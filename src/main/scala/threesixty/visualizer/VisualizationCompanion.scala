package threesixty.visualizer

import threesixty.data.{ InputData, DataPool }
import threesixty.data.metadata.CompleteInputMetadata
import threesixty.engine.UsageInfo
import threesixty.processor.{ProcessingStep, ProcessingMethod, ProcessingStrategy}


/** Trait for companion objects to  [[threesixty.visualizer.Visualization]]. */
trait VisualizationCompanion extends UsageInfo {
    /** Verbose name of the visualization */
    def name: String

    /** Conversion from String to [[threesixty.visualizer.VisualizationConfig]]. */
    def fromString: (String) => VisualizationConfig


    val metadata: VisualizationMetadata


    def degreeOfFit(inputMetadata: CompleteInputMetadata*): Double = ???
    def degreeOfFit(processingStrategy: ProcessingStrategy, inputMetadata: CompleteInputMetadata*): Double = ???


    /**
     *  Method to determine if a list of input data fulfills the requirements of the visualization
     *
     *  @param inputData a list of input data
     *  @param procMeth the processing step
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
