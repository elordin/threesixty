package threesixty.visualizer.visualizations.PieChart

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.visualizer._
import threesixty.config.Config


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationInfo] =
        super.visualizationInfos + ("piechart" ->
            VisualizationInfo(
                "PieChart",
                { json:String => PieChartConfig.apply(json) },
                "Parameters: \n" // TODO
            )
        )
}


object PieChartConfig {


    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): PieChartConfig = new PieChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class PieChart(config: PieChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class PieChartConfig private (
    val ids: Set[Identifier],
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig(ids: Set[Identifier], height, width) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        )))

    def apply(config: Config): PieChartConfig.PieChart = PieChartConfig.PieChart(this, config.getDatasets(ids))

}
