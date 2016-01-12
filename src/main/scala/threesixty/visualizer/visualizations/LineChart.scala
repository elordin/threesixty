package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.visualizer.{Visualization, VisualizationConfig, withVisualizationConversions}


object LineChartConfig {
    trait Conversion extends withVisualizationConversions {
        abstract override def visualizationConversions: Map[String, (String) => VisualizationConfig] =
            super.visualizationConversions + (LineChartConfig.name -> {
                json:String => LineChartConfig.apply(json)
            })
    }

    val name = "LineChart"

    /**
     *  Public constructor that parses JSON into a configuration
     *  @param JSON representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(json: String): LineChartConfig = new LineChartConfig(100, 200) // TODO actually read JSON


    case class LineChart(config: LineChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class LineChartConfig private (
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

    def apply(data: Set[ProcessedData]): LineChartConfig.LineChart = LineChartConfig.LineChart(this)

}
