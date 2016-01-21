package threesixty.visualizer.visualizations.BarChart

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.visualizer._
import threesixty.config.Config


trait Mixin extends VisualizationMixins {
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
 * @author Thomas Engel
 */
object BarChartConfig {

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return BarChartConfig with all arguments from the JSON set
      */
    def apply(json: String): BarChartConfig = new BarChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class BarChart(config: BarChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class BarChartConfig private (
    val ids: Set[Identifier],
       height: Int,
       width: Int,
       xMin: Option[Timestamp] = None,
       xMax: Option[Timestamp] = None,
       yMin: Option[ValueType] = None,
       yMax: Option[ValueType] = None,
       xLabel: String = "",
       yLabel: String = "",
       title: String = ""
) extends VisualizationConfig(ids: Set[Identifier]) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None //TODO Aggregation
        )))

    def apply(config: Config): BarChartConfig.BarChart = BarChartConfig.BarChart(this, config.getDatasets(ids))

}
