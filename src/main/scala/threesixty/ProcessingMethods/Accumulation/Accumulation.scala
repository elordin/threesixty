package threesixty.ProcessingMethods.Accumulation

import spray.json.DefaultJsonProtocol._
import spray.json._
import threesixty.data.Data.Identifier
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.tags.Accumulated
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint, InputDataSkeleton}
import threesixty.processor.{ProcessingMethodCompanion, ProcessingMixins, ProcessingStep, SingleProcessingMethod}
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


object Accumulation extends ProcessingMethodCompanion {

    trait Mixin extends ProcessingMixins {
        abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
            super.processingInfos + ("accumulation" -> Accumulation)
    }

    def name = "Accumulation"

    def fromString: (String) => ProcessingStep = { s => apply(s).asProcessingStep }

    def usage = """ Use responsibly """ // TODO

    def apply(jsonString: String): Accumulation = {
        implicit val accumulationFormat =
            jsonFormat( { idm: Map [Identifier, Identifier] => Accumulation.apply(idm) }, "idMapping")

        jsonString.parseJson.convertTo[Accumulation]
    }

    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep =
        Accumulation(idMapping).asProcessingStep

    def computeDegreeOfFit(inputData: InputDataSkeleton): Double = {

        var temp = 0.0
        val meta = inputData.metadata

        if (meta.scaling == Scaling.Ordinal) {
            temp += 0.4
        }
        if (meta.size >= 5) {
            temp += 0.2
        }
        if (meta.size >= 50) {
            temp += 0.2 //overall 0.4 because >= 50 includes >= 5
        }
        if (meta.resolution == Resolution.High) {
            temp += 0.2
        }
        if (meta.resolution == Resolution.Middle) {
            temp += 0.1
        }

        temp
    }

    def computeDegreeOfFit(targetVisualization: VisualizationConfig, inputData: InputDataSkeleton): Double = {

        val strategyFactor = computeDegreeOfFit(inputData)
        val visFactor = targetVisualization match {
            //good
            case _:LineChartConfig          => 1.0
//             case _:HeatLineChartConfig      => 1.0
            case _:BarChartConfig           => 0.8
//             case _:PolarAreaChartConfig     => 0.8 //equal to BarChar
            //bad
            case _:ScatterChartConfig       => 0.2
//             case _:ScatterColorChartConfig  => 0.2
//             case _:ProgressChartConfig      => 0.1
            case _:PieChartConfig           => 0.0
            //default
            case _                          => 0.5
        }

        strategyFactor * visFactor
    }

}


/**
  *  Accumulator
  *
  *  @author Jens Wöhrle
  */
case class Accumulation(idMapping: Map[Identifier, Identifier])
    extends SingleProcessingMethod {

    def companion: ProcessingMethodCompanion = Accumulation

    /**
      *  Creates a new dataset with ID as specified in idMapping.
      *  Inserts interpolated values along the original ones into
      *  this new dataset and adds tags to identify interpolated
      *  and original values.
      *
      *  @param data Data to interpolate
      *  @return One element Set containing the new dataset
      */
    @throws[NoSuchElementException]("if data.id can not be found in idMapping")
    def apply(data: ProcessedData): Set[ProcessedData] = {

        /**
          * Interpolation function.
          * For each combination of two points it creates the linear
          * equation paramters m (slope) and b (offset).
          * It the generates the appropriate number of intermediary points
          * with the corresponding values and tags and inserts them into
          * the list of datapoints.
          *
          * @param list of datapoints
          * @return list of datapoints with interpolated values and Tnterpolation-tags
          */
        def akkumulated: List[TaggedDataPoint] => List[TaggedDataPoint] = {
            case d1@TaggedDataPoint(t1, v1, tags1) :: (d2@TaggedDataPoint(t2, v2, tags2) :: ds) =>
                TaggedDataPoint(t1, v1, tags1 + Accumulated) :: akkumulated( TaggedDataPoint(t2, v1.value + v2.value, tags2 + Accumulated) :: ds )

            case otherwise => otherwise
        }

        val orderedDataPoints = data.dataPoints.sortBy(d => timestamp2Long(d.timestamp))

        val newID = idMapping(data.id)

        Set(data.copy(id = newID, dataPoints = akkumulated(orderedDataPoints)))
    }
}
