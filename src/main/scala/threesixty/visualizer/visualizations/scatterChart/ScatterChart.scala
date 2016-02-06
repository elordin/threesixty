package threesixty.visualizer.visualizations.scatterChart

import spray.json._
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.metadata.Scaling
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.util._

import scala.xml.Elem
import scala.annotation.tailrec


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("scatterchart" -> ScatterChartConfig)
}


/**
 *  The config class for a [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]].
 *
 *  @author Thomas Engel
 */
object ScatterChartConfig extends VisualizationCompanion {

    def name = "ScatterChart"

    def usage = "ScatterChart\n" +
                "  Parameters: \n" +
                "    ids:                       Set[String]            - The data identifiers\n" +
                "    height:                    Int                    - Height of the diagram in px\n" +
                "    width:                     Int                    - Width of the diagram in px\n" +
                "    border:                    Border      (optional) - Border (top, bottom, left, right) in px\n" +
                "    colorScheme:               String      (optional) - The color scheme\n" +
                "    title:                     String      (optional) - Diagram title\n" +
                "    titleVerticalOffset:       Int         (optional) - The vertical offset of the title\n" +
                "    titleFontSize:             Int         (optional) - The font size of the title\n" +
                "    xlabel:                    String      (optional) - The label for the x-axis\n" +
                "    ylabel:                    String      (optional) - The label for the y-axis\n" +
                "    minDistanceX:              Int         (optional) - The minimum number of px between two grid points on the x-axis\n" +
                "    minDistanceY:              Int         (optional) - The minimum number of px between two grid points on the y-axis\n" +
                "    fontSize:                  Int         (optional) - The font size\n" +
                "    fontFamily:                String      (optional) - The font family\n" +
                "    xMin:                      Double      (optional) - The minimum value displayed on the x-axis\n" +
                "    xMax:                      Double      (optional) - The maximum value displayed on the x-axis\n" +
                "    yMin:                      Double      (optional) - The minimum value displayed on the y-axis\n" +
                "    yMax:                      Double      (optional) - The maximum value displayed on the y-axis\n" +
                "    xUnit:                     Double      (optional) - The unit on the x-axis\n" +
                "    yUnit:                     Double      (optional) - The unit on the y-axis\n" +
                "    radius:                    Int         (optional) - The radius of the displayed points\n"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }


  def default(ids: Seq[Identifier], height: Int, width: Int) = ScatterChartConfig(ids, height, width)
    /**
     *  Public constructor that parses JSON into a configuration
     *  @param jsonString representation of the config
     *  @return ScatterChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): ScatterChartConfig = {
        implicit val scatterChartConfigFormat = jsonFormat(ScatterChartConfig.apply,
            "ids",
            "height", "width",
            "border",
            "colorScheme",
            "title", "titleVerticalOffset", "titleFontSize",
            "xlabel", "ylabel", "minDistanceX", "minDistanceY",
            "fontSize", "fontFamily",
            "xMin", "xMax", "yMin", "yMax",
            "xUnit", "yUnit",
            "radius")
        jsonString.parseJson.convertTo[ScatterChartConfig]
    }

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal)
        )))


    /**
     * This class creates the svg element for a scatter chart.
     *
     * @param config the scatter chart config
     * @param data the data
     *
     * @author Thomas Engel, Thomas Weber
     */
    case class ScatterChart(config: ScatterChartConfig, val data: ProcessedData*) extends Visualization(data: _*) {
        require(data.size == 2, "Scatter chart can only be applied to two sets of data.")

        val joinedDatasets: Seq[List[(TaggedDataPoint, TaggedDataPoint)]] =
            data.tail.map {
                dataset => data.head.equiJoin(dataset, _.timestamp.getTime)
            }


        // TODO Performance optimization, get both in one run
        val dataMinMaxX: (Double, Double) =
            (joinedDatasets.map(_.map({ case (dp: TaggedDataPoint, _: TaggedDataPoint) => dp.value.value}).min).min,
            joinedDatasets.map(_.map({ case (dp: TaggedDataPoint, _: TaggedDataPoint) => dp.value.value}).max).min)
        val dataMinMaxY: (Double, Double) = dataMinMaxX
        val dataMinX: Double = dataMinMaxX._1
        val dataMaxX: Double = dataMinMaxX._2
        val dataMinY: Double = dataMinMaxY._1
        val dataMaxY: Double = dataMinMaxY._2
        val chartOrigin = (config.border.left, config.height - config.border.bottom)

        val xScale = config._xUnit.map(
                ValueScale(config._xMin.getOrElse(dataMinX),
                    config._xMax.getOrElse(dataMaxX), 0, config.chartWidth, _)).getOrElse {
                    ValueScale(config._xMin.getOrElse(dataMinX),
                        config._xMax.getOrElse(dataMaxX), 0, config.chartWidth)
            }
        val yScale = config._yUnit.map(
                ValueScale(config._yMin.getOrElse(dataMinY),
                    config._yMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
                    ValueScale(config._yMin.getOrElse(dataMinY),
                        config._yMax.getOrElse(dataMaxY), 0, config.chartHeight)
            }

        val xAxisLabels: Seq[(String, Int)] = {
            @tailrec
            def construct(v: Double, init: Seq[(String, Int)]): Seq[(String, Int)] = {
                if (v > xScale.inMax) {
                    init
                } else {
                    construct(xScale.nextBreakpoint(v), init ++ Seq((xScale.format(v), xScale(v).toInt)))
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

        def toSVG: Elem = {
            /*
            val displayData = data.map {
                dataset => ProcessedData(dataset.id, dataset.dataPoints.filter {
                    dataPoint =>
                        dataPoint.timestamp.getTime >= xScale.inMin &&
                        dataPoint.timestamp.getTime <= xScale.inMax &&
                        dataPoint.value.value >= yScale.inMin &&
                        dataPoint.value.value <= yScale.inMax
                })
            }
            */
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox

            /*
            val xdata = data.head
            val ydata = data.last

            val zippedData = xdata.dataPoints.zip(ydata.dataPoints)
            */
            (<g id="datapoints">
                { for { dataPoints <- joinedDatasets } yield  {
                    val color = DefaultColorScheme.next
                    for { (dp1, dp2) <- dataPoints } yield
                        //TODO Adapt class when joined datapoints are used
                        <circle
                            class={(dp1.tags ++ dp2.tags).map(_.toString.replace(' ', '_')) mkString " " }
                            cx={ (chartOrigin._1 + xScale(dp1.value.value)).toString }
                            cy={ (chartOrigin._2 - yScale(dp2.value.value)).toString }
                            fill={ color.toHexString }
                            r={config.radius.toString} />
                } }
            </g>: SVGXML)
                .withGrid(Grid(
                    chartOrigin._1,
                    chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xAxisLabels.map(_._2),
                    yAxisLabels.map(_._2)
                    ))
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
 * The config to create a [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]].
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
 *
 * @author Thomas {Engel, Weber}
 */
case class ScatterChartConfig(
    val ids:                        Seq[Identifier],
    val height:                     Int,
    val width:                      Int,
    val _border:                    Option[Border]      = None,
    val _colorScheme:               Option[String]      = None,
    val _title:                     Option[String]      = None,
    val _titleVerticalOffset:       Option[Int]         = None,
    val _titleFontSize:             Option[Int]         = None,
    val _xLabel:                    Option[String]      = None,
    val _yLabel:                    Option[String]      = None,
    val _minPxBetweenXGridPoints:   Option[Int]         = None,
    val _minPxBetweenYGridPoints:   Option[Int]         = None,
    val _fontSize:                  Option[Int]         = None,
    val _fontFamily:                Option[String]      = None,

    val _xMin:                      Option[Double]   = None,
    val _xMax:                      Option[Double]   = None,
    val _yMin:                      Option[Double]      = None,
    val _yMax:                      Option[Double]      = None,
    val _xUnit:                     Option[Double]      = None,
    val _yUnit:                     Option[Double]      = None,
    val _radius:                    Option[Int]         = None
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
    _fontFamily = _fontFamily) {

    /**
      * @return the radius of the points
      */
    def radius: Int = _radius.getOrElse(2)

    require(radius > 0, "Value for radius must be greater than 0.")

    /**
     *  Returns a
     *  [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]]
     *  using visualizing the given dataset with configuration.
     *
     *  @param pool the pool containing the data
     *  @return the [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]] for this configuration
     */
    def apply(pool: DataPool): ScatterChartConfig.ScatterChart =  {
        ScatterChartConfig.ScatterChart(this, pool.getDatasets(ids: _*): _*)
    }

}
