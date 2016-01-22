package threesixty.visualizer.visualizations.PolarAreaChart

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationInfo] =
        super.visualizationInfos + ("polarareachart" ->
            VisualizationInfo(
                "PolarAreaChart",
                { json:String => PolarAreaChartConfig.apply(json) },
                "Parameters: \n" // TODO
            )
        )
}


object PolarAreaChartConfig {


    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): PolarAreaChartConfig = new PolarAreaChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class PolarAreaChart(config: PolarAreaChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class PolarAreaChartConfig private (
    val ids: Set[Identifier],
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig(ids: Set[Identifier]) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredGoal = None //TODO NoGoal
        )))

    def apply(config: Config): PolarAreaChartConfig.PolarAreaChart = PolarAreaChartConfig.PolarAreaChart(this, config.getDatasets(ids))

}
