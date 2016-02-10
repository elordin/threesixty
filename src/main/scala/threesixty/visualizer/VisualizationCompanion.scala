package threesixty.visualizer

import threesixty.data.Data.Identifier
import threesixty.data.{ InputDataSkeleton }
import threesixty.engine.UsageInfo
import threesixty.processor.{ProcessingStep, ProcessingStrategy}
import threesixty.decisionengine.visualizations.VisualizationDecisionMethod


/** Trait for companion objects to  [[threesixty.visualizer.Visualization]]. */
trait VisualizationCompanion extends VisualizationDecisionMethod with UsageInfo  {
    /** Verbose name of the visualization */
    def name: String

    /** Conversion from String to [[threesixty.visualizer.VisualizationConfig]]. */
    def fromString: (String) => VisualizationConfig

    def default(ids: Seq[Identifier], height: Int, width: Int): VisualizationConfig

    val metadata: VisualizationMetadata


    def degreeOfFit(inputMetadata: InputDataSkeleton*): Double = 0
    def degreeOfFit(processingStrategy: ProcessingStrategy, inputMetadata: InputDataSkeleton*): Double = 0

    /**
     *  Method to determine if a list of input data fulfills the requirements of the visualization
     *
     *  @param procMeth the processing step
     *  @param skeletons a list of input data
     *  @return a maybe reordered list of input data that matches the visualization requirement.
     *          It does not return a Boolean!
     */
    def isMatching(procMeth: ProcessingStep, skeletons: InputDataSkeleton*): Option[Seq[InputDataSkeleton]] = {

        if(metadata.unlimitedData) {
           isMatching_unlimited(procMeth, skeletons: _*)

        } else if(metadata.numberOfInputs() != skeletons.size) {
            // Wrong number of data for that visualization
            None
        } else {
            // Determine if any order of input data can be matched to the visualization
            isMatching_limited(procMeth, skeletons: _*)
        }
    }

    private def isMatching_unlimited(procMeth: ProcessingStep, skeletons: InputDataSkeleton*) : Option[Seq[InputDataSkeleton]] = {
        // Unlimited data that all have to match the same data requirement
        var matching = true
        val dataRequirement = metadata.requirementList.head
        for(i <- 0 until skeletons.size) {
            matching = matching && dataRequirement.isMatchingData(skeletons(i), procMeth)
        }
        val result = matching match {
            case true => Some(skeletons)
            case false => None
        }
        result
    }

    private def isMatching_limited(procMeth : ProcessingStep, skeletons: InputDataSkeleton*): Option[Seq[InputDataSkeleton]] = {

        // Build matrix that determines if a specific input data can be matched to a specific data requirement
        val matchingMatrix = buildMatchingMatrix(procMeth, skeletons: _*)

        // Check all permutations of input data if any of them can be matched
        val permutations = List.range(0, skeletons.size ).permutations.toList
        for(p <- 0 until permutations.size) {
             val temp = checkPermutations(permutations, skeletons, matchingMatrix,p)
               temp match {
                   case Some(list) => return Some(list) //force exit and return this value
                   case None => {} //skip
               }
        }
        None //is only reached if return-statement is never met

    }

    private def buildMatchingMatrix(procMeth: ProcessingStep, skeletons: InputDataSkeleton*) = {
        val matchingMatrix = Array.ofDim[Boolean](skeletons.size, metadata.requirementList.size)
        for (i <- 0 until skeletons.size;
             k <- 0 until metadata.requirementList.size) {
            matchingMatrix(i)(k) = metadata.requirementList(k).isMatchingData(skeletons(i), procMeth)
        }

        matchingMatrix
    }

    private  def checkPermutations( permutations : List[List[Int]], skeletons: Seq[InputDataSkeleton],
                                    matchingMatrix: Array[Array[Boolean]], p : Int): Option[Seq[InputDataSkeleton]] ={

        val perm = permutations(p)
        var matching = true
        // Look up in matching matrix
        var requirementIndex = 0
        for(i <- 0 until skeletons.size) {
            val dataIndex = perm(i)
            matching = matching && matchingMatrix(dataIndex)(requirementIndex)
            requirementIndex += 1
        }

        // Build matching order of input data
        if(matching) {
            var result: Seq[InputDataSkeleton] = Seq()
            for(i <- 1 to perm.size) {
                result +:= skeletons(perm.size - i)
            }
            return Some(result) //force break and return this value
        }
        None
    }
}
