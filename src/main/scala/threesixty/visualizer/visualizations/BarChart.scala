package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.visualizer._

/**
 * @author Thomas Engel
 */
object BarChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (BarChartConfig.name -> {
                json:String => BarChartConfig.apply(json)
            })
    }

    val name = "BarChart"

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return BarChartConfig with all arguments from the JSON set
      */
    def apply(json: String): BarChartConfig = new BarChartConfig(100, 200) // TODO actually read JSON


    case class BarChart(config: BarChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class BarChartConfig private (
       height: Int,
       width: Int,
       xMin: Option[Timestamp] = None,
       xMax: Option[Timestamp] = None,
       yMin: Option[ValueType] = None,
       yMax: Option[ValueType] = None,
       xLabel: String = "",
       yLabel: String = "",
       title: String = ""
) extends VisualizationConfig {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None //TODO Aggregation
        )))

    def apply(data: Set[ProcessedData]): BarChartConfig.BarChart = BarChartConfig.BarChart(this)

}
