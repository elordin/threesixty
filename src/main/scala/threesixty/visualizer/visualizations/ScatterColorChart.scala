package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config


object ScatterColorChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("scattercolorchart" ->
                VisualizationInfo(
                    "ScatterColorChart",
                    { json:String => ScatterColorChartConfig.apply(json) },
                    "Parameters: \n" // TODO
                )
            )
    }

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ScatterColorChartConfig = new ScatterColorChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class ScatterColorChart(config: ScatterColorChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: xml.Elem = <svg></svg>
    }
}


case class ScatterColorChartConfig private (
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
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredGoal = None //TODO NoGoal
        )))

    def apply(config: Config): ScatterColorChartConfig.ScatterColorChart =
        ScatterColorChartConfig.ScatterColorChart(this, config.getDatasets(ids))

}
