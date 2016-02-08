package threesixty.ProcessingMethods.interpolation

import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint, InputDataSkeleton}
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.tags.{Tag, Interpolated, Original}
import threesixty.processor.{ProcessingMixins, SingleProcessingMethod, ProcessingMethodCompanion, ProcessingStep}

import spray.json._
import DefaultJsonProtocol._
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig


object LinearInterpolation extends ProcessingMethodCompanion with ProcessingMixins {

    trait Mixin extends ProcessingMixins {
        abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
            super.processingInfos + ("linearinterpolation" -> LinearInterpolation)
    }

    def name = "Linear Interpolation"

    def fromString: (String) => ProcessingStep = { s => apply(s).asProcessingStep }

    def usage =
        """ |Linear Interpolation(frequency, idmap), takes one additional argument.
            |Hereby the frequency is the desired max distance between two points in ms
            |The interpolated points will be tagged with interpolated,the others not
        """.stripMargin

    def apply(jsonString: String): LinearInterpolation = {
        implicit val linearInterpolationFormat =
            jsonFormat(LinearInterpolation.apply, "frequency", "idMapping")
        jsonString.parseJson.convertTo[LinearInterpolation]
    }

    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep =
        LinearInterpolation(1000, idMapping).asProcessingStep

    def computeDegreeOfFit(inputData: InputDataSkeleton): Double = {

        var temp = 0.0
        val meta = inputData.metadata

        if (meta.scaling == Scaling.Ordinal) {
            temp += 0.4
        }
        if (meta.size >= 5) {
            temp += 1.0
        }
        if (meta.size >= 50) {
            temp += 0.6 //overall 0.4 because >= 50 includes >= 5
        }
        if (meta.resolution == Resolution.High) {
            temp += 0.0
        }
        if (meta.resolution == Resolution.Middle) {
            temp += 1.0
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
            case _:PieChartConfig           => 0.1
            //default
            case _                          => 0.5
        }

        strategyFactor * visFactor
    }

}


/**
 *  Linear interpolator
 *
 *  @author Thomas Weber
 *  @param frequency Desired max. time-distance between datapoints.
 */
case class LinearInterpolation(frequency: Int, idMapping: Map[Identifier, Identifier])
    extends SingleProcessingMethod {

    def companion: ProcessingMethodCompanion = LinearInterpolation
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
        def linearInterpolated: List[TaggedDataPoint] => List[TaggedDataPoint] = {
            case d1@TaggedDataPoint(t1, v1, tags1) :: (d2@TaggedDataPoint(t2, v2, tags2) :: ds) =>

                if (t2 - t1 > frequency) {

                    val m = ((v2.value - v1.value) / (t2 - t1))
                    val b = v1.value - m * t1

                    def interpolFunc(x: Long): TaggedDataPoint =
                        TaggedDataPoint(new Timestamp(x), m * x + b, Set[Tag](Interpolated))


                    val diff: Int = (t2 - t1).toInt

                    TaggedDataPoint(t1, v1, tags1 + Original) ::
                        (for { i <- 1 to diff / frequency } yield { interpolFunc(t1 + i * frequency) }).toList ++
                        linearInterpolated(TaggedDataPoint(t2, v2, tags2 + Original) :: ds)

                } else {
                    TaggedDataPoint(t1, v1, tags1 + Original) ::
                      linearInterpolated(TaggedDataPoint(t2, v2, tags2 + Original) :: ds)
                }

            case otherwise => otherwise
        }

        val orderedDataPoints = data.dataPoints.sortBy(d => timestamp2Long(d.timestamp))

        val newID = idMapping(data.id)

        Set(data.copy(id = newID, dataPoints = linearInterpolated(orderedDataPoints)))
    }
}
