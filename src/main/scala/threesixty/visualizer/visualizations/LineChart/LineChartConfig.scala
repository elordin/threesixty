package threesixty.visualizer.visualizations.lineChart

import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.metadata.Scaling
import threesixty.data.{ProcessedData, TaggedDataPoint, DataPool}
import threesixty.visualizer.{
    DataRequirement,
    Visualization,
    VisualizationCompanion,
    VisualizationConfig,
    VisualizationMetadata,
    VisualizationMixins}
import threesixty.visualizer.util._

import threesixty.visualizer.SVGXML

import scala.xml.Elem
import scala.concurrent.duration.FiniteDuration
import scala.annotation.tailrec

import spray.json._


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("linechart" -> LineChartConfig)
}


object LineChartConfig extends VisualizationCompanion {

    def name = "LineChart"

    def usage = "LineChart\n" +
                "  Parameters: \n" +
                "    ids:               List[String]         - The data identifiers\n" +
                "    height:            Int                  - Height of the diagram in px\n" +
                "    width:             Int                  - Width of the diagram in px\n" +
                "    xMin:              Timestamp (optional) - Minimum value of the x-axis\n" +
                "    xMax:              Timestamp (optinmal) - Maximum value of the x-axis\n" +
                "    yMin:              Double    (optional) - Minimum value of the y-axis\n" +
                "    yMax:              Double    (optional) - Maximum value of the y-axis\n" +
                "    xLabel:            String    (optional) - Label for the x-axis\n" +
                "    yLabel:            String    (optional) - Label for the y-axis\n" +
                "    title:             String    (optional) - Diagram title\n" +
                "    borderTop:         Int       (optional) - Border to the top in px\n" +
                "    borderBottom:      Int       (optional) - Border to the bottom in px\n" +
                "    borderLeft:        Int       (optional) - Border to the left in px\n" +
                "    borderRight:       Int       (optional) - Border to the right in px\n" +
                "    distanceTitle      Int       (optional) - Distance between the title and the chart in px\n" +
                "    minDistanceX       Int       (optional) - Minimum number of px between two control points on the x-axis\n" +
                "    minDistanceY       Int       (optional) - Minimum number of px between two control points on the y-axis\n" +
                "    xUnit              String    (optional) - Name of the desired unit on the x-axis\n" +
                "    yUnit              Double    (optional) - Value of the desired unit on the y-axis\n" +
                "    fontSizeTitle      Int       (optional) - Font size of the title\n" +
                "    fontSize           Int       (optional) - Font size of labels\n"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }


    /**
     *  Public constructor that parses JSON into a LineChartConfig
     *  @param jsonString representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): LineChartConfig = {
        implicit val lineChartConfigFormat = jsonFormat(LineChartConfig.apply,
            "ids", "height", "width", "xMin", "xMax", "yMin", "yMax",
            "xLabel", "yLabel", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "distanceTitle", "minDistanceX", "minDistanceY",
            "xUnit", "yUnit", "fontSizeTitle", "fontSize")
        jsonString.parseJson.convertTo[LineChartConfig]
    }

    case class LineChart private[LineChartConfig] (
        config: LineChartConfig,
        data: Set[ProcessedData]
    ) extends Visualization(data: Set[ProcessedData]) {

        private def doubleFold[T](projection: (TaggedDataPoint) => T, selection: (T, T) => T): T =
            (projection(data.head.dataPoints.head) /: data)({
                (currResult: T, dataset: ProcessedData) =>
                    val resultOfSet: T = (projection(dataset.dataPoints.head) /: dataset.dataPoints)({
                            (a: T, b: TaggedDataPoint) => selection(a, projection(b))
                        })
                    selection(resultOfSet, currResult)
            })

        val dataMinMaxX: (Long, Long) =
            doubleFold[(Long, Long)](
                { dp => (dp.timestamp.getTime, dp.timestamp.getTime) },
                { case ((a, b), (c, d)) => (if (a < c) a else c, if (a > c) a else c) })
        val dataMinMaxY: (Double, Double) =
            doubleFold[(Double, Double)](
                { dp => (dp.value.value, dp.value.value) },
                { case ((a, b), (c, d)) => (if (a < c) a else c, if (a > c) a else c) })

        val dataMinX: Long = dataMinMaxX._1
        val dataMaxX: Long = dataMinMaxX._2
        val dataMinY: Double = dataMinMaxY._1
        val dataMaxY: Double = dataMinMaxY._2

        private def scaleToFitX(value: Long): Int =
            ((value - dataMinX) / (dataMaxX - dataMinX)).toInt * config.chartWidth
        private def scaleToFitY(value: Double): Int =
            ((value - dataMinY) / (dataMaxY - dataMinY)).toInt * config.chartHeight

        private def xAxisLabels(formatter: (Long) => String): Seq[(String, Int)] = {
            def bestFittingUnit(start: Long, end: Long) = ???

            val step: FiniteDuration = config.optUnitX.getOrElse {
                bestFittingUnit(dataMaxX, dataMinX)
            }

            def smallestBreakPoint(start: Long, step: FiniteDuration): Long = ???

            @tailrec
            def construct(t: Long, init: Seq[(String, Int)]): Seq[(String, Int)] = {
                if (t > dataMaxX) {
                    init
                } else {
                    construct(t + step.toMillis, init ++ Seq((formatter(t), scaleToFitX(t))))
                }
            }
            construct(smallestBreakPoint(dataMinX, step), Seq())
        }

        // TODO
        val yAxisLabels: Seq[(String, Int)] = ??? // TODO

        private def calculatePath(data: ProcessedData): String =
            'M' + data.dataPoints.foldLeft("")({
                (s, dp) => s + s"L${scaleToFitX(dp.timestamp.getTime)} ${scaleToFitY(dp.value.value)} "
            }).tail

        def toSVG: Elem = {
            val xLabels = xAxisLabels({ a => a.toString })
            val yLabels = yAxisLabels
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox

            (<g class="data">
                { for { dataset <- data } yield {
                    val color: RGBColor = ColorScheme.next
                    <g class={ s"datapoints-${dataset.id}" }>
                        {
                            for { datapoint <- dataset.dataPoints } yield {
                                <circle
                                    fill={ color.toString }
                                    stroke={ color.toString }
                                    cx={ scaleToFitX(datapoint.timestamp.getTime).toString }
                                    cy={ scaleToFitY(datapoint.value.value).toString }
                                    r="4" />
                            }
                        }
                    </g>
                    <path
                        stroke={ color.toString }
                        fill="none"
                        stroke-width="2"
                        d={ calculatePath(dataset) } />
                } }
            </g>: SVGXML)
                .withGrid(Grid(
                    config.chartOrigin._1,
                    config.chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xLabels.size,
                    yLabels.size))
                .withAxis(HorizontalAxis(
                    x = config.chartOrigin._1,
                    y = config.chartOrigin._2,
                    width = config.chartWidth,
                    title = config.xLabel,
                    labels = xLabels))
                .withAxis(VerticalAxis(
                    x = config.chartOrigin._1,
                    y = config.chartOrigin._2,
                    height = config.chartHeight,
                    title = config.yLabel,
                    labels = yLabels))
                .withTitle(config.title, 1, 2, config.fontSizeTitle)
                .withSVGHeader(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight)
        }

    }
}


case class LineChartConfig(
    val ids:            Set[Identifier],
    val height:         Int,
    val width:          Int,
    val optXMin:        Option[Timestamp] = None,
    val optXMax:        Option[Timestamp] = None,
    val optYMin:        Option[Double]    = None,
    val optYMax:        Option[Double]    = None,
    val _xLabel:        Option[String]    = None,
    val _yLabel:        Option[String]    = None,
    val _title:         Option[String]    = None,
    val _borderTop:     Option[Int]       = None,
    val _borderBottom:  Option[Int]            = None,
    val _borderLeft:    Option[Int]            = None,
    val _borderRight:   Option[Int]            = None,
    val _distanceTitle: Option[Int]            = None,
    val _minDistanceX:  Option[Int]            = None,
    val _minDistanceY:  Option[Int]            = None,
    val optUnitX:       Option[FiniteDuration] = None,
    val optUnitY:       Option[Double]         = None,
    val _fontSizeTitle: Option[Int]            = None,
    val _fontSize:      Option[Int]            = None
) extends VisualizationConfig(
    ids,
    height,
    width,
    _title,
    _borderTop,
    _borderBottom,
    _borderLeft,
    _borderRight,
    _distanceTitle,
    _fontSizeTitle,
    _fontSize
) {

    def xLabel: String = _xLabel.getOrElse("")
    def yLabel: String = _yLabel.getOrElse("")

    def minDistanceX: Int = _minDistanceX.getOrElse(20)
    def minDistanceY: Int = _minDistanceY.getOrElse(20)

    require(minDistanceX > 0, "Value for minDistanceX must be greater than 0.")
    require(minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    val metadata = new VisualizationMetadata(
        List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    def apply(pool: DataPool): LineChartConfig.LineChart = {
        LineChartConfig.LineChart(this, pool.getDatasets(ids))
    }
}
