package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config


object PolarAreaChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("polarareachart" ->
                VisualizationInfo(
                    "PolarAreaChart",
                    { json:String => PolarAreaChartConfig.apply(json) },
                    "Parameters: \n" // TODO
                )
            )
    }


    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): PolarAreaChartConfig = new PolarAreaChartConfig(100, 200) // TODO actually read JSON


    case class PolarAreaChart(config: PolarAreaChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class PolarAreaChartConfig private (
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredGoal = None //TODO NoGoal
        )))

    def apply(config: Config): PolarAreaChartConfig.PolarAreaChart = PolarAreaChartConfig.PolarAreaChart(this)

}
