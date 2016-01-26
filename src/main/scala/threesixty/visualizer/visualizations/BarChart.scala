package threesixty.visualizer.visualizations.barChart

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.visualizer._
import threesixty.config.Config

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("barchart" -> BarChartConfig)
}


/**
 * @author Thomas Engel
 */
object BarChartConfig extends VisualizationCompanion {

    def name = "BarChart"

    def usage = "BarChart\n" +
                "  Parameters: \n" // TODO

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return BarChartConfig with all arguments from the JSON set
      */
    def apply(json: String): BarChartConfig = new BarChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class BarChart(config: BarChartConfig, data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def getSVGElements: List[Elem] = ???
    }
}


case class BarChartConfig(
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
) extends VisualizationConfig(ids: Set[Identifier], height, width) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None //TODO Aggregation
        )))

    def apply(config: Config): BarChartConfig.BarChart = BarChartConfig.BarChart(this, config.getDatasets(ids))

}
