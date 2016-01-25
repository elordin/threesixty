package threesixty.visualizer

import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier
import threesixty.processor.ProcessingStrategy
import threesixty.config.Config
import threesixty.engine.UsageInfo

import spray.json._
import DefaultJsonProtocol._


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
) extends Function1[Config, Visualization] {

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



/** Trait for companion objects to  [[threesixty.visualizer.Visualization]]. */
trait VisualizationCompanion extends UsageInfo {
    /** Verbose name of the visualization */
    def name: String

    /** Conversion from String to [[threesixty.visualizer.VisualizationConfig]]. */
    def fromString: (String) => VisualizationConfig
}


/**
 *  Mixin trait for layering [[threesixty.visualizer.Visualization]]s onto the [[threesixty.visualizer.Visualizer]]
 *
 *  Extend this by abstract overriding the visualizationInfos value with super calls.
 *  @example {{{
 *      object FooVisualizationConfig extends VisualizationCompanion {
 *          def name = "Foo"
 *          def fromString(in: String) = ...
 *          def usage = "Use responsibly!"
 *      }
 *
 *      trait FooVisualizationMixin extends VisualizationMixins {
 *          abstract override def visualizationInfos =
 *              super.visualizationInfos + ("foo" -> FooVisualizationConfig)
 *      }
 *
 *      val visualizer = new Visualizer with FooVisualizationMixin
 *  }}}
 */
trait VisualizationMixins {
    /**
     *  Map containing all mixedin Visualizations.
     *  Use an abstract override to extends this.
     */
    def visualizationInfos: Map[String, VisualizationCompanion] = Map.empty
}


/**
 *  Holds a list of available visualizations and some meta information,
 *  including how to convert to [[threesixty.visualizer.VisualizationConfig]].
 *
 *  Stack traits inheriting from [[threesixty.visualizer.VisualizationMixins]] to add visualizations.
 *
 *  @author Thomas Weber
 *
 *  @example {{{
 *      val visualizer = new Visualizer with FooVisualization.Mixin with BarVisualization.Mixin
 *  }}}
 */
class Visualizer extends VisualizationMixins with UsageInfo {
    // TODO Exception catching and proper access

    def usage = " ... "


    @throws[IllegalArgumentException]("if a parameter is missing")
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
            ).fromString

        val args: String = json.fields.get("args") match {
            case Some(jsonVal) => jsonVal.toString // get args from visualization
            case None          => throw new IllegalArgumentException("parameter \"args\" missing for visualization")
        }

        conversion(args)
    }

}
