package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.visualizer._
import threesixty.config.Config


/**
 * @author Thomas Engel
 */
object BarChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("barchart" ->
                VisualizationInfo(
                    "BarChart",
                    { json:String => BarChartConfig.apply(json) },
                    "Parameters: \n"
                    // TODO
                )
            )
    }


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

    def apply(config: Config): BarChartConfig.BarChart = BarChartConfig.BarChart(this)

}
