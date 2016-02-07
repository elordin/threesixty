package threesixty.ProcessingMethods.Aggregation

import spray.json.DefaultJsonProtocol._
import spray.json._
import threesixty.ProcessingMethods.statistics.StatisticalAnalysis
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.tags._
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint, InputDataSkeleton}
import threesixty.processor.{ProcessingMethodCompanion, ProcessingMixins, ProcessingStep, SingleProcessingMethod}
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


object Aggregation extends ProcessingMethodCompanion {

    trait Mixin extends ProcessingMixins {
        abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
            super.processingInfos + ("aggregation" -> Aggregation)
    }

    def name = "Aggregation"

    def fromString: (String) => ProcessingStep = { s => apply(s).asProcessingStep }

    def usage = """ Use responsibly """ // TODO

    def apply(jsonString: String): Aggregation = {
        implicit val aggregationFormat =
            jsonFormat(Aggregation.apply, "mode", "param", "idMapping")
        jsonString.parseJson.convertTo[Aggregation]
    }

    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep =
        Aggregation("mean", "d-1000", idMapping).asProcessingStep

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
            // case _:HeatLineChartConfig      => 1.0
            case _:BarChartConfig           => 0.8
            // case _:PolarAreaChartConfig     => 0.8 //equal to BarChar
            //bad
            case _:ScatterChartConfig       => 0.2
            // case _:ScatterColorChartConfig  => 0.2
            // case _:ProgressChartConfig      => 0.1
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
  *  @param mode possible input:
  *                 mean:       In the aggrggaton the MEAN is used
  *                 sum         In the aggrggaton the values are summed up and its sum is returned
  *                 num         In the aggregation the number of values is used - its number of elements is used
  *                 enum        This aggregation goes by the Y-Axis: and counts how often which value occured; param not need in this case
  *
  * @param param possible input:
  *                 datasize-1990:
  *                 blocksize-1990:
  *                 weekday:
  *                 monthly:    Aggregates the values of a month together
  *                 yearly:     Aggregates the values of a year together
  *
  *
  */ //groupby() bei Listen :-)
case class Aggregation(mode: String, param: String, idMapping: Map[Identifier, Identifier])
    extends SingleProcessingMethod {

    def companion: ProcessingMethodCompanion = Aggregation
    /**
      *  Creates a new dataset with ID as specified in idMapping.
      *  Creates new Dataset, but with aggregated Data which can directly used for
      *  the diagramm as the number of data has been reduced
      *  Tag aggegated will be added as well
      *     Timeaggregated -> just reduce the complexity of the datapoints
      *
      *
      *
      *  @param data Data to interpolate
      *  @return One element Set containing the new dataset
      */
    @throws[NoSuchElementException]("if data.id can not be found in idMapping")
    def apply(data: ProcessedData): Set[ProcessedData] = {
        val paramsplit = param.split("-").map(_.trim)
        if( param == "" ) {
            throw new IllegalArgumentException("Empty Argument not allow in param")
        }

        if( paramsplit(0) == "datasize" || paramsplit(0) == "blocksize" ) {
            if( paramsplit.length != 2) {
                throw new IllegalArgumentException("Wrong Input, needs to be of format datasize-190")
            }

            var agdata = data.dataPoints.sortBy(d => -timestamp2Long((d.timestamp)))

            var blocksize = 0
            var datasize = 0

            paramsplit(0) match {
                case "datasize" =>
                    datasize = paramsplit(1).toInt
                    blocksize = math.ceil(agdata.length/datasize).toInt
                case "blocksize" =>
                    blocksize = paramsplit(1).toInt
                    datasize = math.ceil(agdata.length/datasize).toInt
                case default =>
                    throw new IllegalArgumentException("Not matching argument given like 'datasize' or 'blocksize' BUT got: " + paramsplit(0) )
            }

            var l = List[TaggedDataPoint]()

            for( i <- 0 until datasize ) {
                val buf = agdata.splitAt(blocksize)
                val r = buf._1
                agdata = buf._2
                l = TaggedDataPoint( r(0).timestamp, StatisticalAnalysis.mean(r), r(0).tags + TimeAggregated) :: l
            }

            val newID = idMapping(data.id)

            Set(ProcessedData(newID, l))
        } else {

            val grouped = param match {
                case "minute" =>
                    data.dataPoints.groupBy( _.timestamp.getMinutes )
                case "hour" =>
                    data.dataPoints.groupBy( _.timestamp.getHours )
                case "weekday" =>
                    data.dataPoints.groupBy( _.timestamp.getDay )
                case "day" =>
                    data.dataPoints.groupBy( _.timestamp.getDate )
                case "month" =>
                    data.dataPoints.groupBy( _.timestamp.getMonth )
                case "year" =>
                    data.dataPoints.groupBy( _.timestamp.getYear )
                case "enum" =>
                    data.dataPoints.groupBy( _.value.value )
                case default =>
                    throw new IllegalArgumentException("Not matching argument given like 'minute', 'hour', 'weekday', 'month', 'year' BUT got: " + param )
            }

            var l = List[TaggedDataPoint]()

            for( (k,v) <- grouped ) {
                mode match {
                    case "num" =>
                        l = TaggedDataPoint(new Timestamp(0), v.length, v(0).tags + new AggregationTag("" + v(0).timestamp.getDay)) :: l
                    case "mean" =>
                        l = TaggedDataPoint(new Timestamp(0), StatisticalAnalysis.mean(v), v(0).tags + new AggregationTag("" + v(0).timestamp.getDay)) :: l
                    case "sum" =>
                        l = TaggedDataPoint(new Timestamp(0), StatisticalAnalysis.sum(v), v(0).tags + new AggregationTag("" + v(0).timestamp.getDay)) :: l
                    case "enum" =>
                        l = TaggedDataPoint(new Timestamp(0), v.length, v(0).tags + new AggregationTag("" + v(0).value)) :: l
                    case default =>
                        throw new IllegalArgumentException("Not matching argument given like 'num', 'mean', 'sum' BUT got: " + param )
                }
            }

            val newID = idMapping(data.id)

            Set( ProcessedData(newID, l) )
        }
    }
}
