package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._


object ScatterChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (ScatterChartConfig.name -> {
                json:String => ScatterChartConfig.apply(json)
            })
    }

    val name = "ScatterChart"

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ScatterChartConfig = new ScatterChartConfig(100, 200) // TODO actually read JSON


    case class ScatterChart(config: ScatterChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class ScatterChartConfig private (
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
        )))

    def apply(data: Set[ProcessedData]): ScatterChartConfig.ScatterChart = ScatterChartConfig.ScatterChart(this)

}
