package threesixty.visualizer.visualizations.scatterChart

import threesixty.data.{ProcessedData, DataPool}
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("scatterchart" -> ScatterChartConfig)
}


object ScatterChartConfig extends VisualizationCompanion {

    def name = "ScatterChart"

    def usage = "ScatterChart\n" +
                "  Parameters: \n" // TODO

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ScatterChartConfig = new ScatterChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class ScatterChart(config: ScatterChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def getSVGElements: List[Elem] = ???
    }
}


case class ScatterChartConfig(
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
        )))

    def apply(pool: DataPool): ScatterChartConfig.ScatterChart = ScatterChartConfig.ScatterChart(this, pool.getDatasets(ids))

}
