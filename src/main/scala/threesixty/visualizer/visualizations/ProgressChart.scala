package threesixty.visualizer.visualizations.progressChart

import threesixty.data.{ProcessedData, DataPool}
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("progresschart" -> ProgressChartConfig)
}


object ProgressChartConfig extends VisualizationCompanion {

    def name = "ProgressChart"

    def usage = "ProgressChart\n" +
                "  Parameters: \n" // TODO

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): ProgressChartConfig = new ProgressChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class ProgressChart(config: ProgressChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def getSVGElements: List[Elem] = ???
    }
}


case class ProgressChartConfig private (
    val ids: Set[Identifier],
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig(ids: Set[Identifier], height, width) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredProcessingMethods = None, //TODO Accumulation
            requiredGoal = None //TODO SingleValueGoal
        )))

    def apply(pool: DataPool): ProgressChartConfig.ProgressChart =
        ProgressChartConfig.ProgressChart(this, pool.getDatasets(ids))

}
