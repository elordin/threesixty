package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._


object ScatterColorChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (ScatterColorChartConfig.name -> {
                json:String => ScatterColorChartConfig.apply(json)
            })
    }

    val name = "ScatterColorChart"

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ScatterColorChartConfig = new ScatterColorChartConfig(100, 200) // TODO actually read JSON


    case class ScatterColorChart(config: ScatterColorChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class ScatterColorChartConfig private (
    height: Int,
    width: Int,
    xMin: Option[Timestamp] = None,
    xMax: Option[Timestamp] = None,
    yMin: Option[ValueType] = None,
    yMax: Option[ValueType] = None,
    xLabel: String = "",
    yLabel: String = "",
    title: String = ""
) extends VisualizationConfig {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredGoal = None //TODO NoGoal
        )))

    def apply(data: Set[ProcessedData]): ScatterColorChartConfig.ScatterColorChart =
        ScatterColorChartConfig.ScatterColorChart(this)

}
