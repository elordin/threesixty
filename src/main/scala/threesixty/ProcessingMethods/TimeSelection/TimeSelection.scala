package threesixty.ProcessingMethods.TimeSelection

import spray.json._
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint, InputDataSkeleton}
import threesixty.processor.{ProcessingMethodCompanion, ProcessingMixins, ProcessingStep, SingleProcessingMethod}
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


object TimeSelection extends ProcessingMethodCompanion {

    trait Mixin extends ProcessingMixins {
        abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
            super.processingInfos + ("timeselection" -> TimeSelection)
    }

    def name = "TimeSelection"

    def fromString: (String) => ProcessingStep = { s => apply(s).asProcessingStep }

    def usage = """ Use responsibly """ // TODO

    def apply(jsonString: String): TimeSelection = {
        implicit val timeselectionFormat =
            jsonFormat(TimeSelection.apply, "from", "to", "idMapping")
        jsonString.parseJson.convertTo[TimeSelection]
    }

    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep =
        TimeSelection(new Timestamp(0), new Timestamp(0), idMapping).asProcessingStep

    def computeDegreeOfFit(inputData: InputDataSkeleton): Double = {

        var temp = 0.0
        val meta = inputData.metadata

        if (meta.scaling == Scaling.Ordinal) {
            temp += 0.4
        }
        if (inputData.metadata.size >= 5) {
            temp += 0.2
        }
        if (inputData.metadata.size >= 50) {
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
            case _:BarChartConfig           => 0.8
            //bad
            case _:ScatterChartConfig       => 0.2
            case _:PieChartConfig           => 0.0
            //default
            case _                          => 0.5
        }

        strategyFactor * visFactor
    }

}


/**
  *  Timeframe Selection
  *
  *  @author Jens Wöhrle
  *  @param from Timestamp from which day
  *  @param to Timestamp until which day
  *
  *
  */ //groupby() bei Listen :-)
case class TimeSelection(from: Timestamp, to: Timestamp, idMapping: Map[Identifier, Identifier])
    extends SingleProcessingMethod {

    def companion: ProcessingMethodCompanion = TimeSelection

    /**
      *  Creates a new dataset with ID as specified in idMapping.
      *  Creates new Dataset reduced to its time frame, defined in the arguments
      *
      *  @param data Data to interpolate
      *  @return One element Set containing the new dataset
      */
    @throws[NoSuchElementException]("if data.id can not be found in idMapping")
    def apply(data: ProcessedData): Set[ProcessedData] = {
        val l = data.dataPoints.filter(  { dt: TaggedDataPoint => dt.timestamp <= to && from <= dt.timestamp } )
        val newID = idMapping(data.id)
        Set( ProcessedData(newID, l))
    }
}
