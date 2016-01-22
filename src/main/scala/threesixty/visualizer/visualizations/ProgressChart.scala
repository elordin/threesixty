package threesixty.visualizer.visualizations.ProgressChart

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationInfo] =
        super.visualizationInfos + ("progresschart" ->
            VisualizationInfo(
                "ProgressChart",
                { json:String => ProgressChartConfig.apply(json) },
                "Parameters: \n" // TODO
            )
        )
}


object ProgressChartConfig {

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ProgressChartConfig = new ProgressChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class ProgressChart(config: ProgressChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class ProgressChartConfig private (
    val ids: Set[Identifier],
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig(ids: Set[Identifier], height, width) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredProcessingMethods = None, //TODO Accumulation
            requiredGoal = None //TODO SingleValueGoal
        )))

    def apply(config: Config): ProgressChartConfig.ProgressChart = ProgressChartConfig.ProgressChart(this, config.getDatasets(ids))

}
