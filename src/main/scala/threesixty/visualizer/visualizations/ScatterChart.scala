package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config


object ScatterChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("scatterchart" ->
                VisualizationInfo(
                    "ScatterChart",
                    { json:String => ScatterChartConfig.apply(json) },
                    "Parameters: \n" // TODO
                )
            )
    }


    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ScatterChartConfig = new ScatterChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class ScatterChart(config: ScatterChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class ScatterChartConfig private (
    val ids: Set[Identifier],
    height: Int,
    width: Int,
    xMin: Option[Timestamp] = None,
    xMax: Option[Timestamp] = None,
    yMin: Option[ValueType] = None,
    yMax: Option[ValueType] = None,
    xLabel: String = "",
    yLabel: String = "",
    title: String = ""
) extends VisualizationConfig(ids: Set[Identifier]) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal)
        )))

    def apply(config: Config): ScatterChartConfig.ScatterChart = ScatterChartConfig.ScatterChart(this, config.getDatasets(ids))

}
