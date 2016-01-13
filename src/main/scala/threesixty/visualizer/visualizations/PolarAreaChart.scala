package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.metadata.Scaling
import threesixty.visualizer._


object PolarAreaChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (PolarAreaChartConfig.name -> {
                json:String => PolarAreaChartConfig.apply(json)
            })
    }

    val name = "PolarAreaChart"

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

    def apply(data: Set[ProcessedData]): PolarAreaChartConfig.PolarAreaChart = PolarAreaChartConfig.PolarAreaChart(this)

}
