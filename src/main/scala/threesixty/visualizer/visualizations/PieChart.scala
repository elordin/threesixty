package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.visualizer._


object PieChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (PieChartConfig.name -> {
                json:String => PieChartConfig.apply(json)
            })
    }

    val name = "PieChart"

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): PieChartConfig = new PieChartConfig(100, 200) // TODO actually read JSON


    case class PieChart(config: PieChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class PieChartConfig private (
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        )))

    def apply(data: Set[ProcessedData]): PieChartConfig.PieChart = PieChartConfig.PieChart(this)

}
