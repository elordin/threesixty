package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.visualizer._
import threesixty.config.Config


object HeatLineChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("heatlinechart" ->
                VisualizationInfo(
                    "HeatLineChart",
                    { json:String => HeatLineChartConfig.apply(json) },
                    "Parameters: \n" // TODO
                )
            )
    }


    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): HeatLineChartConfig = new HeatLineChartConfig(100, 200) // TODO actually read JSON


    case class HeatLineChart(config: HeatLineChartConfig) extends Visualization {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class HeatLineChartConfig private (
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
            resolution = Some(Resolution.High)
        )))

    def apply(config: Config): HeatLineChartConfig.HeatLineChart = HeatLineChartConfig.HeatLineChart(this)

}
