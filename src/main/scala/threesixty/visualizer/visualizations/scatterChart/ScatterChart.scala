package threesixty.visualizer.visualizations.scatterChart

import spray.json._
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.metadata.Scaling
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.util._
import ColorScheme.ColorSchemeJsonFormat
import threesixty.visualizer.util.param._

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
                "    title:                     Title       (optional) - Diagram title (title, position, verticalOffset, horizontalOffset, size, fontFamily, alignment)\n" +
                "    xAxis:                     ValueAxis   (optional) - The x-axis (label, labelSize, labelFontFamily, min, max, minDistance, unit, unitLabelSize, unitLabelFontFamily, showGrid, showLabels, arrowSize, arrowFilled)\n" +
                "    yAxis:                     ValueAxis   (optional) - The y-axis (label, labelSize, labelFontFamily, min, max, minDistance, unit, unitLabelSize, unitLabelFontFamily, showGrid, showLabels, arrowSize, arrowFilled)\n" +
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
            "colorScheme", "title",
            "xAxis", "yAxis",
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

        val _xMin = config._xAxis.map(_.min).getOrElse(None)
        val _xMax = config._xAxis.map(_.max).getOrElse(None)
        val _yMin = config._yAxis.map(_.min).getOrElse(None)
        val _yMax = config._yAxis.map(_.max).getOrElse(None)

        val _xUnit = config._xAxis.map(_.unit).getOrElse(None)
        val _yUnit = config._yAxis.map(_.unit).getOrElse(None)

        val xScale = _xUnit.map(
                ValueScale(_xMin.getOrElse(dataMinX),
                    _xMax.getOrElse(dataMaxX), 0, config.chartWidth, _)).getOrElse {
                    ValueScale(_xMin.getOrElse(dataMinX),
                        _xMax.getOrElse(dataMaxX), 0, config.chartWidth)
            }
        val yScale = _yUnit.map(
                ValueScale(_yMin.getOrElse(dataMinY),
                    _yMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
                    ValueScale(_yMin.getOrElse(dataMinY),
                        _yMax.getOrElse(dataMaxY), 0, config.chartHeight)
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
            val (xtitle, ytitle) = config.getTitleCoordinates

            /*
            val xdata = data.head
            val ydata = data.last

            val zippedData = xdata.dataPoints.zip(ydata.dataPoints)
            */
            (<g id="datapoints">
                { for { dataPoints <- joinedDatasets } yield  {
                    val color = DefaultColorScheme.next
                    for { (dp1, dp2) <- dataPoints } yield
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
                    xPositions = if(config.xAxis.showGrid) xAxisLabels.map(_._2) else Seq(),
                    yPositions = if(config.yAxis.showGrid) yAxisLabels.map(_._2) else Seq()
                    ))
                .withAxis(HorizontalAxis(
                    x = chartOrigin._1,
                    y = chartOrigin._2,
                    width = config.chartWidth,
                    title = config.xAxis.label,
                    titleSize = config.xAxis.labelSize,
                    titleFontFamily = config.xAxis.labelFontFamily,
                    labels = if(config.xAxis.showLabels) xAxisLabels else Seq(),
                    labelSize = config.xAxis.unitLabelSize,
                    labelFontFamily = config.xAxis.unitLabelFontFamily,
                    arrowSize = config.xAxis.arrowSize,
                    arrowFilled = config.xAxis.arrowFilled))
                .withAxis(VerticalAxis(
                    x = chartOrigin._1,
                    y = chartOrigin._2,
                    height = config.chartHeight,
                    title = config.yAxis.label,
                    titleSize = config.yAxis.labelSize,
                    titleFontFamily = config.yAxis.labelFontFamily,
                    labels = if(config.yAxis.showLabels) yAxisLabels else Seq(),
                    labelSize = config.yAxis.labelSize,
                    labelFontFamily = config.yAxis.labelFontFamily,
                    arrowSize = config.yAxis.arrowSize,
                    arrowFilled = config.yAxis.arrowFilled))
                .withTitle(
                    config.title.title,
                    xtitle,
                    ytitle,
                    config.title.size,
                    config.title.fontFamily,
                    config.title.alignment)
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
 * @param _xAxis the x-axis
 * @param _yAxis the y-axis
 * @param _radius the radius of the points
 *
 * @author Thomas {Engel, Weber}
 */
case class ScatterChartConfig(
    val ids:                        Seq[Identifier],
    val height:                     Int,
    val width:                      Int,
    val _border:                    Option[Border]              = None,
    val _colorScheme:               Option[ColorScheme]         = None,
    val _title:                     Option[OptTitleParam]       = None,

    val _xAxis:                     Option[OptValueAxisParam]   = None,
    val _yAxis:                     Option[OptValueAxisParam]   = None,
    val _radius:                    Option[Int]                 = None
) extends VisualizationConfig(
    ids = ids,
    height = height,
    width = width,
    _border = _border,
    _colorScheme = _colorScheme,
    _title = _title,
    _xAxis = _xAxis,
    _yAxis = _yAxis
) {

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
