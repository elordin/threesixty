package threesixty.algorithms.interpolation

import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint}
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.tags.{Aggregated, Tag, Interpolated, Original}
import threesixty.processor.{ProcessingMixins, SingleProcessingMethod, ProcessingMethodCompanion, ProcessingStep}
import threesixty.algorithms.statistics.StatisticalAnalysis

import spray.json._
import DefaultJsonProtocol._
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.heatLineChart.HeatLineChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.polarAreaChart.PolarAreaChartConfig
import threesixty.visualizer.visualizations.progressChart.ProgressChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig
import threesixty.visualizer.visualizations.scatterColorChart.ScatterColorChartConfig


object Aggregation extends ProcessingMethodCompanion {

    trait Mixin extends ProcessingMixins {
        abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
            super.processingInfos + ("aaggregation" -> Aggregation)
    }

    def name = "Aggregation"

    def fromString: (String) => ProcessingStep = { s => apply(s).asProcessingStep }

    def usage = """ Use responsibly """ // TODO

    def apply(jsonString: String): Aggregation = {
        implicit val aggregationFormat =
            jsonFormat(Aggregation.apply, "frequency", "idMapping")
        jsonString.parseJson.convertTo[Aggregation]
    }

    def computeDegreeOfFit(inputData: InputData): Double = {

        var temp = 0.0
        val meta = inputData.metadata

        if (meta.scaling == Scaling.Ordinal) {
            temp += 0.4
        }
        if (inputData.dataPoints.length >= 5) {
            temp += 0.2
        }
        if (inputData.dataPoints.length >= 50) {
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

    def computeDegreeOfFit(inputData: InputData, targetVisualization: VisualizationConfig ): Double = {

        val strategyFactor = computeDegreeOfFit(inputData)
        val visFactor = targetVisualization match {
            //good
            case _:LineChartConfig          => 1.0
            case _:HeatLineChartConfig      => 1.0
            case _:BarChartConfig           => 0.8
            case _:PolarAreaChartConfig     => 0.8 //equal to BarChar
            //bad
            case _:ScatterChartConfig       => 0.2
            case _:ScatterColorChartConfig  => 0.2
            case _:ProgressChartConfig      => 0.1
            case _:PieChartConfig           => 0.0
            //default
            case _                          => 0.5
        }

        strategyFactor * visFactor
    }

}


/**
  *  Aggregator
  *
  *  @author Jens Wöhrle
  *  @param aggregationMode desired Data points as result
  *                 positive Value: It will just seperate the data in numData Blocks and gives its mean out
  *                 -1: Will aggregate on a daily basis
  *                 -2: EnumAggregation
  */ //groupby() bei Listen :-)
case class Aggregation(aggregationMode: Int, idMapping: Map[Identifier, Identifier])
    extends SingleProcessingMethod(idMapping: Map[Identifier, Identifier]) {

    /**
      *  Creates a new dataset with ID as specified in idMapping.
      *  Creates new Dataset, but with aggregated Data which can directly used for
      *  the diagramm as the number of data has been reduced
      *  Tag aggegated will be added as well
      *
      *
      *  @param data Data to interpolate
      *  @return One element Set containing the new dataset
      */
    @throws[NoSuchElementException]("if data.id can not be found in idMapping")
    def apply(data: ProcessedData): Set[ProcessedData] = {
        if( aggregationMode > 0 ) {
            val agdata = data.dataPoints.sortBy(d => -timestamp2Long((d.timestamp)))

            val agregsize = math.ceil(agdata.length/aggregationMode).toInt

            var l = List[TaggedDataPoint]()

            for( i <- 0 until aggregationMode) {
                var l = agdata.drop(agregsize)
                    l = TaggedDataPoint(StatisticalAnalysis.mean()) : l
            }

            val newID = idMapping
        } else if( aggregationMode == -1) {
        }
        Set()
    }
}
