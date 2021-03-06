package threesixty.ProcessingMethods.Aggregation

import spray.json.DefaultJsonProtocol._
import spray.json._
import threesixty.ProcessingMethods.statistics.StatisticalAnalysis
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.tags._
import threesixty.data.{ProcessedData, TaggedDataPoint, InputDataSkeleton}
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

    def usage =     """ |Aggregator(mode, param). It takes 2 arguments, which will be specified as described and aggregates according to its parameter the data
                        |mode possible input:
                        |  *                 mean:       In the aggrggaton the MEAN is used.
                        |  *                 sum         In the aggrggaton the values are summed up and its sum is returned
                        |  *                 num         In the aggregation the number of values is used - its number of elements is used
                        |  *                 enum        This aggregation goes by the Y-Axis: and counts how often which value occured; param not need in this case
                        |  *
                        |  * param possible input:
                        |  *                 datasize-1990: The number can be any other as well. Aggregates the data that you have 1900 datapoints at the end
                        |  *                 blocksize-1990: The number can be any other as well. Aggregates all the time the next 1900 datapoints together
                        |  *                 minute:     Aggregates the values of a minute together
                        |  *                 hour:
                        |  *                 weekday:    Cyclic Aggregation on Monday, tuesdays, etc.
                        |  *                 day:
                        |  *                 monthl:    Aggregates the values of a month together
                        |  *                 yearl:     Aggregates the values of a year together""".stripMargin

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
            temp += 1.0
        }
        if (meta.size >= 5) {
            temp += 1.0
        }
        if (meta.size >= 50) {
            temp += 1.0 //overall 0.4 because >= 50 includes >= 5
        }
        if (meta.resolution == Resolution.High) {
            temp += 1.0
        }
        if (meta.resolution == Resolution.Middle) {
            temp += 0.6
        }

        temp/4.0 //scale to intervall[0;1]
    }

    def computeDegreeOfFit(targetVisualization: VisualizationConfig, inputData: InputDataSkeleton): Double = {

        val strategyFactor = computeDegreeOfFit(inputData)
        val visFactor = targetVisualization match {
            //good
            case _:LineChartConfig          => 1.0
            case _:BarChartConfig           => 1.0
            //bad
            case _:ScatterChartConfig       => 0.1
            case _:PieChartConfig           => 0.4
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
  * @param param possible input:
  *                 datasize-1990:
  *                 blocksize-1990:
  *                 minute:     Aggregates the values of a minute together
  *                 hour:       "
  *                 weekday:    Cyclic Aggregation on Monday, tuesdays, etc
  *                 day:        "
  *                 monthl:    Aggregates the values of a month together
  *                 yearl:     Aggregates the values of a year together
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
      *  @param data Data to interpolate
      *  @return One element Set containing the new dataset
      */
    @throws[NoSuchElementException]("if data.id can not be found in idMapping")
    def apply(data: ProcessedData): Set[ProcessedData] = {
        //Splitting second argument as it can contain two information
        //datasize-1900 will become {datasize, 1900}
        //minute, etc will remain minute etc
        val paramsplit = param.split("-").map(_.trim)
        if( param == "" ) {
            throw new IllegalArgumentException("Empty Argument not allow in param")
        }

        //Aggregation making "smaller data" and reduces the amount of points
        //Makes datasize blocks with blocksize aggregated points on each new point
        if( paramsplit(0) == "datasize" || paramsplit(0) == "blocksize" ) {
            if( paramsplit.length != 2) {
                throw new IllegalArgumentException("Wrong Input, needs to be of format datasize-190")
            }

            var agdata = data.dataPoints.sortBy(d => -timestamp2Long((d.timestamp)))

            var blocksize = 1
            var datasize = 1

            paramsplit(0) match {
                case "datasize" =>
                    datasize = paramsplit(1).toInt

                    require( datasize != 0, { datasize = 1; println("Datasize value of 0 is nt allowed, takes default 1")})

                    blocksize = math.ceil(agdata.length/datasize).toInt
                case "blocksize" =>
                    blocksize = paramsplit(1).toInt

                    require( blocksize != 0, { blocksize = 1; println("Blocksize value of 0 is nt allowed, takes default 1")})

                    datasize = math.ceil(agdata.length/blocksize).toInt
                case default =>
                    throw new IllegalArgumentException("Not matching argument given like 'datasize' or 'blocksize' BUT got: " + paramsplit(0) )
            }

            //Creating the Aggregation buy
            var l = List[TaggedDataPoint]()

            for( i <- 0 until datasize ) {
                val buf = agdata.splitAt(blocksize)
                val r = buf._1
                agdata = buf._2
                l = TaggedDataPoint( r(0).timestamp, StatisticalAnalysis.mean(r), r(0).tags + TimeAggregated) :: l
            }

            val newID = idMapping(data.id)

            Set(ProcessedData(newID, l))
        //Aggregation on timeframe or cyclic
        } else {

            val grouped = param match {
                case "minute" =>
                    data.dataPoints.groupBy({ d => (d.timestamp.getYear, d.timestamp.getMonth, d.timestamp.getDate, d.timestamp.getHours, d.timestamp.getMinutes) }).toList.sortBy(_._1).reverse
                case "hour" =>
                    data.dataPoints.groupBy({ d => (d.timestamp.getYear, d.timestamp.getMonth, d.timestamp.getDate, d.timestamp.getHours) }).toList.sortBy(_._1).reverse
                case "weekday" =>
                    data.dataPoints.groupBy({ d => d.timestamp.getDay }).toList.sortBy(_._1).reverse
                case "day" =>
                    data.dataPoints.groupBy({ d => (d.timestamp.getYear, d.timestamp.getMonth, d.timestamp.getDate) }).toList.sortBy(_._1).reverse
                case "month" =>
                    data.dataPoints.groupBy({ d => (d.timestamp.getYear, d.timestamp.getMonth) }).toList.sortBy(_._1).reverse
                case "year" =>
                    data.dataPoints.groupBy( _.timestamp.getYear ).toList.sortBy(_._1).reverse
                case "enum" =>
                    data.dataPoints.groupBy( _.value.value ).toList.sortBy(_._1).reverse
                case default =>
                    throw new IllegalArgumentException("Not matching argument given like 'minute', 'hour', 'weekday', 'month', 'year' BUT got: " + param )
            }

            /**
              *  From the aggregated value it creates a proper String for the Charts
              *
              *  @param v Value on which has been aggregated,
              *  @return String represation for a proper label on the value v on which has been aggregated
              */
            def AggregationInfo(v: Double): String = {
                var ret = ""
                if( param == "weekday" ) {
                    v match {
                        case 0 => ret = "Sunday"
                        case 1 => ret = "Monday"
                        case 2 => ret = "Tuesday"
                        case 3 => ret = "Wednesday"
                        case 4 => ret = "Thursday"
                        case 5 => ret = "Friday"
                        case 6 => ret = "Saturday"
                    }
                } else if ( param == "month") {
                    v match {
                        case 0 => ret = "January"
                        case 1 => ret = "February"
                        case 2 => ret = "March"
                        case 3 => ret = "April"
                        case 4 => ret = "May"
                        case 5 => ret = "June"
                        case 6 => ret = "July"
                        case 7 => ret = "August"
                        case 8 => ret = "September"
                        case 9 => ret = "October"
                        case 10 => ret = "November"
                        case 11 => ret = "December"
                    }
                } else {
                    ret = v.toString
                }
                ret
            }

            var l = List[TaggedDataPoint]()

            for( (k,v) <- grouped ) {
                mode match {
                    case "num" =>
                        l = TaggedDataPoint(new Timestamp(0), v.length, v(0).tags + new AggregationTag(AggregationInfo(v(0).timestamp.getDay))) :: l
                    case "mean" =>
                        l = TaggedDataPoint(new Timestamp(0), StatisticalAnalysis.mean(v), v(0).tags + new AggregationTag(AggregationInfo(v(0).timestamp.getDay))) :: l
                    case "sum" =>
                        l = TaggedDataPoint(new Timestamp(0), StatisticalAnalysis.sum(v), v(0).tags + new AggregationTag(AggregationInfo(v(0).timestamp.getDay))) :: l
                    case "enum" =>
                        l = TaggedDataPoint(new Timestamp(0), v.length, v(0).tags + new AggregationTag(AggregationInfo(v(0).value.value))) :: l
                    case default =>
                        throw new IllegalArgumentException("Not matching argument given like 'num', 'mean', 'sum' BUT got: " + param )
                }
            }

            val newID = idMapping(data.id)

            Set( ProcessedData(newID, l) )
        }
    }
}
