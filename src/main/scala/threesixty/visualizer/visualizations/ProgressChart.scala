package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.metadata.Scaling
import threesixty.visualizer._


object ProgressChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (ProgressChartConfig.name -> {
                json:String => ProgressChartConfig.apply(json)
            })
    }

    val name = "ProgressChart"

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

    def apply(data: Set[ProcessedData]): ProgressChartConfig.ProgressChart = ProgressChartConfig.ProgressChart(this)

}
