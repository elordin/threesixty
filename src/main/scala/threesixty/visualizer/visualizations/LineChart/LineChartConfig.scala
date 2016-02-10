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
import ColorScheme.ColorSchemeJsonFormat

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
 *  @author Thomas {Engel, Weber}
 */
object LineChartConfig extends VisualizationCompanion {

    def name = "LineChart"

    def usage = "LineChart\n" +
                "  Parameters: \n" +
                "    ids:                       Set[String]            - The data identifiers\n" +
                "    height:                    Int                    - Height of the diagram in px\n" +
                "    width:                     Int                    - Width of the diagram in px\n" +
                "    border:                    Border      (optional) - Border (top, bottom, left, right) in px\n" +
                "    colorScheme:               String      (optional) - The color scheme\n" +
                "    title:                     String      (optional) - Diagram title\n" +
                "    titleVerticalOffset:       Int         (optional) - The vertical offset of the title\n" +
                "    titleFontSize:             Int         (optional) - The font size of the title\n" +
                "    xLabel:                    String      (optional) - The label for the x-axis\n" +
                "    ylabel:                    String      (optional) - The label for the y-axis\n" +
                "    minDistanceX:              Int         (optional) - The minimum number of px between two grid points on the x-axis\n" +
                "    minDistanceY:              Int         (optional) - The minimum number of px between two grid points on the y-axis\n" +
                "    fontSize:                  Int         (optional) - The font size\n" +
                "    fontFamily:                String      (optional) - The font family\n" +
                "    xMin:                      Timestamp   (optional) - The minimum value displayed on the x-axis\n" +
                "    xMax:                      Timestamp   (optional) - The maximum value displayed on the x-axis\n" +
                "    yMin:                      Double      (optional) - The minimum value displayed on the y-axis\n" +
                "    yMax:                      Double      (optional) - The maximum value displayed on the y-axis\n" +
                "    xUnit:                     String      (optional) - The unit on the x-axis\n" +
                "    yUnit:                     Double      (optional) - The unit on the y-axis\n" +
                "    radius:                    Int         (optional) - The radius of the displayed points\n" +
                "    lineStrokeWidth:           Int         (optional) - The stroke width of the line connecting the points"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    def default(ids: Seq[Identifier], width: Int, height: Int) =
        LineChartConfig(ids,height,width)

    /**
     *  Public constructor that parses JSON into a LineChartConfig
     *  @param jsonString representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): LineChartConfig = {
        implicit val lineChartConfigFormat = jsonFormat(LineChartConfig.apply,
            "ids",
            "height", "width",
            "border",
            "colorScheme",
            "title", "titleVerticalOffset", "titleFontSize",
            "xLabel", "yLabel", "minDistanceX", "minDistanceY",
            "fontSize", "fontFamily",
            "xMin", "xMax", "yMin", "yMax",
            "xUnit", "yUnit",
            "radius", "lineStrokeWidth")
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
        data: ProcessedData*
    ) extends Visualization(data: _*) {

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
        val chartOrigin = (config.border.left, config.height - config.border.bottom)

        val xScale = config._xUnit.map(
                TimeScale(config._xMin.map(_.getTime).getOrElse(dataMinX),
                    config._xMax.map(_.getTime).getOrElse(dataMaxX), 0, config.chartWidth, _)).getOrElse {
                    TimeScale(config._xMin.map(_.getTime).getOrElse(dataMinX),
                        config._xMax.map(_.getTime).getOrElse(dataMaxX), 0, config.chartWidth)
            }
        val yScale = config._yUnit.map(
                ValueScale(config._yMin.getOrElse(dataMinY),
                    config._yMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
                    ValueScale(config._yMin.getOrElse(dataMinY),
                        config._yMax.getOrElse(dataMaxY), 0, config.chartHeight)
            }

        val xAxisLabels: Seq[(String, Int)] = {
            @tailrec
            def construct(t: Long, init: Seq[(String, Int)]): Seq[(String, Int)] = {
                if (t > xScale.inMax) {
                    init
                } else {
                    construct(xScale.nextBreakpoint(t), init ++ Seq((xScale.format(t), xScale(t).toInt)))
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
                    construct(yScale.nextBreakpoint(v), init ++ Seq((yScale.format(v), yScale(v).toInt)))
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
                    val colorString: String = config.colorScheme.next.toString
                    <g class={dataset.id}>
                        {if (config.radius > 0)
                            <g class={s"datapoints-${dataset.id}"}>
                                {for {datapoint <- dataset.dataPoints} yield {
                                    <circle
                                    class={datapoint.tags.map(_.toString.replace(' ', '_')) mkString " "}
                                    fill={colorString}
                                    stroke={colorString}
                                    cx={(chartOrigin._1 + xScale(datapoint.timestamp.getTime)).toString}
                                    cy={(chartOrigin._2 - yScale(datapoint.value.value)).toString}
                                    r={config.radius.toString}/>
                            }}
                            </g>
                        }
                        {if(config.lineStrokeWidth > 0)
                            <path
                            stroke={colorString}
                            fill="none"
                            stroke-width={config.lineStrokeWidth.toString}
                            d={calculatePath(dataset)}/>
                        }
                    </g>
                } }
            </g>: SVGXML)
                .withGrid(Grid(
                    chartOrigin._1,
                    chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xAxisLabels.map(_._2),
                    yAxisLabels.map(_._2)))
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
                .withTitle(
                    config.title,
                    config.width / 2,
                    config.border.top - config.titleVerticalOffset,
                    config.titleFontSize,
                    config.fontFamily)
                .withSVGHeader(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight)
        }

    }
}

/**
 * The config to create a [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]].
 *
 * @param ids ids set of ids which are to be displayed in the visualization
 * @param height the height
 * @param width the width
 * @param _border the border
 * @param _colorScheme the color scheme
 * @param _title the title
 * @param _titleVerticalOffset the vertical offset of the title
 * @param _titleFontSize the font size of the title
 * @param _xLabel the x-axis label
 * @param _yLabel the y-axis label
 * @param _minPxBetweenXGridPoints the minimum distance in px between two grid points on the x-axis
 * @param _minPxBetweenYGridPoints the minimum distance in px between two grid points on the y-axis
 * @param _fontSize the font size of labels
 * @param _fontFamily the font family of labels
 * @param _xMin the minimum value displayed on the x-coordinate
 * @param _xMax the maximum value displayed on the x-coordinate
 * @param _yMin the minimum value displayed on the y-coordinate
 * @param _yMax the maximum value displayed on the y-coordinate
 * @param _xUnit the unit of the x-axis
 * @param _yUnit the unit of the y-axis
 * @param _radius the radius of the points
 * @param _lineStrokeWidth the stroke width of the line
 *
 * @author Thomas {Engel, Weber}
 */
case class LineChartConfig(
    val ids:                        Seq[Identifier],
    val height:                     Int,
    val width:                      Int,
    val _border:                    Option[OptBorder]      = None,
    val _colorScheme:               Option[ColorScheme] = None,
    val _title:                     Option[String]      = None,
    val _titleVerticalOffset:       Option[Int]         = None,
    val _titleFontSize:             Option[Int]         = None,
    val _xLabel:                    Option[String]      = None,
    val _yLabel:                    Option[String]      = None,
    val _minPxBetweenXGridPoints:   Option[Int]         = None,
    val _minPxBetweenYGridPoints:   Option[Int]         = None,
    val _fontSize:                  Option[Int]         = None,
    val _fontFamily:                Option[String]      = None,

    val _xMin:                      Option[Timestamp]   = None,
    val _xMax:                      Option[Timestamp]   = None,
    val _yMin:                      Option[Double]      = None,
    val _yMax:                      Option[Double]      = None,
    val _xUnit:                     Option[String]      = None,
    val _yUnit:                     Option[Double]      = None,
    val _radius:                    Option[Int]         = None,
    val _lineStrokeWidth:           Option[Int]         = None
) extends VisualizationConfig(
    ids = ids,
    height = height,
    width = width,
    _border = _border,
    _colorScheme = _colorScheme,
    _title = _title,
    _titleVerticalOffset = _titleVerticalOffset,
    _titleFontSize = _titleFontSize,
    _xLabel = _xLabel,
    _yLabel = _yLabel,
    _minPxBetweenXGridPoints = _minPxBetweenXGridPoints,
    _minPxBetweenYGridPoints = _minPxBetweenYGridPoints,
    _fontSize = _fontSize,
    _fontFamily = _fontFamily
) {

    /**
     * @return the radius of the points
     */
    def radius: Int = _radius.getOrElse(2)

    /**
     * @return the stroke width of the line
     */
    def lineStrokeWidth: Int = _lineStrokeWidth.getOrElse(2)

    /**
     *  Sets the [[Grid]] and returns the
     *  [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]]
     *  for this configuration.
      *
     *  @param pool the pool containing the data
     *  @return the [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]] for this configuration
      */
    def apply(pool: DataPool): LineChartConfig.LineChart = {
        LineChartConfig.LineChart(this, pool.getDatasets(ids: _*): _*)
    }
}
