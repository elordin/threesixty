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
import scala.annotation.tailrec

import spray.json._


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("linechart" -> LineChartConfig)
}


/**
 *  The config class for a [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]].
 *
 *  @author Thomas Engel, Thomas Weber
 */
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

    val metadata = new VisualizationMetadata(
        List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    /**
     *
     *  @param config the line chart config
     *  @param data the data
     *
     *  @author Thomas Engel
     */
    case class LineChart private[LineChartConfig] (
        config: LineChartConfig,
        data: Set[ProcessedData]
    ) extends Visualization(data: Set[ProcessedData]) {

        // TODO Performance optimization, get both in one run
        val dataMinMaxX: (Long, Long) =
            (data.map({ d => d.dataPoints.map({ p => p.timestamp.getTime }).min }).min,
            data.map({ d => d.dataPoints.map({ p => p.timestamp.getTime }).max }).max)
        val dataMinMaxY: (Double, Double) =
            (data.map({ d => d.dataPoints.map({ p => p.value.value }).min }).min,
            data.map({ d => d.dataPoints.map({ p => p.value.value }).max }).max)
        val dataMinX: Long = dataMinMaxX._1
        val dataMaxX: Long = dataMinMaxX._2
        val dataMinY: Double = dataMinMaxY._1
        val dataMaxY: Double = dataMinMaxY._2
        val chartOrigin = (config.borderLeft, config.height - config.borderBottom)

        val xScale = config.optUnitX.map(
                TimeScale(config.optXMin.map(_.getTime).getOrElse(dataMinX),
                    config.optXMax.map(_.getTime).getOrElse(dataMaxX), 0, config.chartWidth, _)).getOrElse {
                    TimeScale(config.optXMin.map(_.getTime).getOrElse(dataMinX),
                        config.optXMax.map(_.getTime).getOrElse(dataMaxX), 0, config.chartWidth)
            }
        val yScale = config.optUnitY.map(
                ValueScale(config.optYMin.getOrElse(dataMinY),
                    config.optYMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
                    ValueScale(config.optYMin.getOrElse(dataMinY),
                        config.optYMax.getOrElse(dataMaxY), 0, config.chartHeight)
            }

        val xAxisLabels: Seq[(String, Int)] = {
            @tailrec
            def construct(t: Long, init: Seq[(String, Int)]): Seq[(String, Int)] = {
                if (t > xScale.inMax) {
                    init
                } else {
                    construct(xScale.nextBreakpoint(t), init ++ Seq((xScale.format(t), xScale(t))))
                }
            }
            construct(xScale.nextBreakpoint(xScale.inMin), Seq())
        }

        val yAxisLabels: Seq[(String, Int)] =  {
            @tailrec
            def construct(v: Double, init: Seq[(String, Int)]): Seq[(String, Int)] = {
                if (v > yScale.inMax) {
                    init
                } else {
                    construct(yScale.nextBreakpoint(v), init ++ Seq((yScale.format(v), yScale(v))))
                }
            }
            construct(yScale.nextBreakpoint(yScale.inMin), Seq())
        }


        private def calculatePath(data: ProcessedData): String =
            'M' + data.dataPoints.foldLeft("")({
                (s, dp) => s + s"L${chartOrigin._1 + xScale(dp.timestamp.getTime)} ${chartOrigin._2 - yScale(dp.value.value)} "
            }).tail

        def toSVG: Elem = {
            val displayData = data.map {
                dataset => ProcessedData(dataset.id, dataset.dataPoints.filter {
                    dataPoint =>
                        dataPoint.timestamp.getTime >= xScale.inMin &&
                        dataPoint.timestamp.getTime <= xScale.inMax &&
                        dataPoint.value.value >= yScale.inMin &&
                        dataPoint.value.value <= yScale.inMax
                })
            }
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox

            (<g class="data">
                { for { dataset <- displayData } yield {
                    val color: RGBColor = ColorScheme.next
                    <g class={ s"datapoints-${dataset.id}" }>
                        {
                            for { datapoint <- dataset.dataPoints } yield {
                                <circle
                                    fill={ color.toString }
                                    stroke={ color.toString }
                                    cx={ (chartOrigin._1 + xScale(datapoint.timestamp.getTime)).toString }
                                    cy={ (chartOrigin._2 - yScale(datapoint.value.value)).toString }
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
                    chartOrigin._1,
                    chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xScale(xScale.step),
                    xScale(xScale.step),
                    xOffset = xScale(xScale.nextBreakpoint(dataMinX)),
                    yOffset = yScale(yScale.nextBreakpoint(dataMinY))))
                .withAxis(HorizontalAxis(
                    x = chartOrigin._1,
                    y = chartOrigin._2,
                    width = config.chartWidth,
                    title = config.xLabel,
                    labels = xAxisLabels))
                .withAxis(VerticalAxis(
                    x = chartOrigin._1,
                    y = chartOrigin._2,
                    height = config.chartHeight,
                    title = config.yLabel,
                    labels = yAxisLabels))
                .withTitle(config.title, config.width / 2, config.borderTop - config.distanceTitle, config.fontSizeTitle)
                .withSVGHeader(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight)
        }

    }
}


/**
 *  The config to create a [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]].
 *
 *  @param ids set of ids which are to be displayed in the visualization
 *  @param height the height
 *  @param width the width
 *  @param optXMin the minimum value displayed on the x-coordinate
 *  @param optXMax the maximum value displayed on the x-coordinate
 *  @param optYMin the minimum value displayed on the y-coordinate
 *  @param optYMax the maximum value displayed on the y-coordinate
 *  @param _xLabel the label on the x-axis
 *  @param _yLabel the label on the y-axis
 *  @param _title the title
 *  @param _borderTop the border to the top
 *  @param _borderBottom the border to the bottom
 *  @param _borderLeft the border to the left
 *  @param _borderRight the border to the right
 *  @param _distanceTitle the distance between the title and the top of the chart
 *  @param _minDistanceX the minimal distance between two grid points on the x-axis
 *  @param _minDistanceY the minimal distance between two grid points on the y-axis
 *  @param optUnitX the unit of the x-axis
 *  @param optUnitY the unit of the y-axis
 *  @param _fontSizeTitle the font size of the title
 *  @param _fontSize the font size of labels
 *
 *  @author Thomas Engel, Thomas Weber
 */
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
    val _borderBottom:  Option[Int]       = None,
    val _borderLeft:    Option[Int]       = None,
    val _borderRight:   Option[Int]       = None,
    val _distanceTitle: Option[Int]       = None,
    val _minDistanceX:  Option[Int]       = None,
    val _minDistanceY:  Option[Int]       = None,
    val optUnitX:       Option[String]    = None,
    val optUnitY:       Option[Double]    = None,
    val _fontSizeTitle: Option[Int]       = None,
    val _fontSize:      Option[Int]       = None
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

    /**
     *  Sets the [[Grid]] and returns the
     *  [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]]
     *  for this configuration.
      *
     *  @param pool the pool containing the data
     *  @return the [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]] for this configuration
      */
    def apply(pool: DataPool): LineChartConfig.LineChart = {
        LineChartConfig.LineChart(this, pool.getDatasets(ids))
    }
}
