package threesixty.visualizer.visualizations.scatterColorChart

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._
import threesixty.config.Config

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("scattercolorchart" -> ScatterColorChartConfig)
}


object ScatterColorChartConfig extends VisualizationCompanion {

    def name = "ScatterColorChart"

    def usage = "ScatterColorChart\n" +
                "  Parameters: \n" // TODO

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ScatterColorChartConfig = new ScatterColorChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class ScatterColorChart(config: ScatterColorChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def getSVGElements: List[Elem] = ???
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
) extends VisualizationConfig(ids: Set[Identifier], height, width) {
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
