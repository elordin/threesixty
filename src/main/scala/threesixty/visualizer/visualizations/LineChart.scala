package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.visualizer.{
        Visualization,
        VisualizationConfig,
        withVisualizationInfos,
        VisualizationInfo,
        DataRequirement,
        VisualizationMetadata
    }
import threesixty.data.metadata.Scaling
import threesixty.config.Config


object LineChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("linechart" ->
                VisualizationInfo(
                    "LineChart",
                    { json:String => LineChartConfig.apply(json) },
                    "Parameters: \n" +
                    "    height: Int    - Height of the diagram in px\n" +
                    "    width:  Int    - Width of the diagram in px\n" +
                    "    xMin:   Int    - Minimum value of the x-axis\n" +
                    "    xMax:   Int    - Maximum value of the x-axis\n" +
                    "    yMin:   Int    - Minimum value of the y-axis\n" +
                    "    yMax:   Int    - Maximum value of the y-axis\n" +
                    "    xLabel: String - Label for the x-axis\n" +
                    "    yLabel: String - Label for the y-axis\n" +
                    "    title:  String - Diagram title\n"
                )
            )
    }


    /**
     *  Public constructor that parses JSON into a configuration
     *  @param json representation of the config
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
    val metadata = new VisualizationMetadata(
            List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    def apply(config: Config): LineChartConfig.LineChart = LineChartConfig.LineChart(this)

}
