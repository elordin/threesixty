package threesixty.visualizer

import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier
import threesixty.config.Config
import threesixty.engine.UsageInfo

import spray.json._
import DefaultJsonProtocol._


abstract class VisualizationConfig(ids: Set[Identifier]) extends Function1[Config, Visualization] {
    val metadata: VisualizationMetadata

    /**
      *  Method to determine if a list of input data fulfills the requirements of the visualization
      *
      *  @param inputData a list of input data
      *  @param config the configuration
      *  @return a maybe reordered list of input data that matches the visualization requirement
      */
    def isMatching(inputData: List[InputData], config: Config): Option[List[InputData]] = {
        if(metadata.unlimitedData) {
            // Unlimited data that all have to match the same data requirement
            var matching = true
            val dataRequirement = metadata.dataRequirement.head
            for(i <- 0 until inputData.size) {
                matching = matching && dataRequirement.isMatchingData(inputData(i), config)
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
                    matchingMatrix(i)(k) = metadata.dataRequirement(k).isMatchingData(inputData(i), config)
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


case class VisualizationInfo(
    val name: String,
    val conversion: (String) => VisualizationConfig,
    val usage: String
) extends UsageInfo


trait withVisualizationInfos {
    def visualizationInfos: Map[String, VisualizationInfo] = Map.empty
}


/**
 *  Visualizer allows to convert a definition of a desired output
 *  visualization into a fitting VisualizationConfig.
 *
 *  Stack traits derived from withVisualizationConversions to add
 *  further possible visualizations.
 *
 *  @author Thomas Weber
 *
 *  @example {{{
 *      val visualizer = new Visualizer with LineChartConfig.Info with PieChartConfig.Info
 *  }}}
 */
class Visualizer extends withVisualizationInfos {

    @throws[IllegalArgumentException]("if the json specifies a type that has no conversion")
    @throws[NoSuchElementException]("if the json specifies a type that has no conversion")
    def toVisualizationConfig(jsonString: String): VisualizationConfig = {
        val json: JsObject = jsonString.parseJson.asJsObject
        val vizType = try {
            json.getFields("type")(0).convertTo[String]
        } catch {
            case e:IndexOutOfBoundsException =>
                throw new IllegalArgumentException("parameter \"type\" missing for visualization")
        }

        val conversion: (String) => VisualizationConfig =
            this.visualizationInfos.getOrElse(vizType,
                throw new NoSuchElementException(s"Unknown visualization type $vizType")
            ).conversion

        val args: String = try {
            json.getFields("args")(0).convertTo[String] // get args from visualization
        } catch {
            case e:IndexOutOfBoundsException =>
                throw new IllegalArgumentException("parameter \"args\" missing for visualization")
        }

        conversion(args)
    }

}
