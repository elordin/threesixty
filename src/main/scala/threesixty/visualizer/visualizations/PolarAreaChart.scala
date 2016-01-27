package threesixty.visualizer.visualizations.polarAreaChart

import threesixty.data.{ProcessedData, DataPool}
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.data.metadata.Scaling
import threesixty.visualizer._

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("polarareachart" -> PolarAreaChartConfig)
}


object PolarAreaChartConfig extends VisualizationCompanion {

    def name = "PolarAreaChart"

    def usage = "PolarAreaChart\n" +
                "  Parameters: \n" // TODO

    def fromString: (String) => VisualizationConfig = { s => apply(s) }


    /**
      *  Public constructor that parses JSON into a configuration
      *  @param json representation of the config
      *  @return LineChartConfig with all arguments from the JSON set
      */
    def apply(json: String): PolarAreaChartConfig = new PolarAreaChartConfig(Set(), 100, 200) // TODO actually read JSON


    case class PolarAreaChart(config: PolarAreaChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def getSVGElements: List[Elem] = ???
    }
}


case class PolarAreaChartConfig private (
    val ids: Set[Identifier],
    height: Int,
    width: Int,
    title: String = ""
) extends VisualizationConfig(ids: Set[Identifier], height, width) {
    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal),
            requiredGoal = None //TODO NoGoal
        )))

    def apply(pool: DataPool): PolarAreaChartConfig.PolarAreaChart =
        PolarAreaChartConfig.PolarAreaChart(this, pool.getDatasets(ids))

}
