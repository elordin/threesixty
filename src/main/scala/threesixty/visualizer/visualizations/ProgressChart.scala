package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config


object ProgressChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("progresschart" ->
                VisualizationInfo(
                    "ProgressChart",
                    { json:String => ProgressChartConfig.apply(json) },
                    "Parameters: \n" // TODO
                )
            )
    }

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ProgressChartConfig = new ProgressChartConfig(100, 200) // TODO actually read JSON


    case class ProgressChart(config: ProgressChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class ProgressChartConfig private (
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredProcessingMethods = None, //TODO Accumulation
            requiredGoal = None //TODO SingleValueGoal
        )))

    def apply(config: Config): ProgressChartConfig.ProgressChart = ProgressChartConfig.ProgressChart(this)

}
