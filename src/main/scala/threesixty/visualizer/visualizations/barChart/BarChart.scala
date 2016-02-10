package threesixty.visualizer.visualizations.barChart

import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{Tag, AggregationTag}
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.util._
import threesixty.visualizer.util.param.{OptAxisParam, OptValueAxisParam, OptTitleParam, OptBorder}
import threesixty.visualizer.visualizations.BarElement
import ColorScheme.ColorSchemeJsonFormat

import spray.json._

import scala.annotation.tailrec
import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("barchart" -> BarChartConfig)
}


/**
 * The config class for a [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]].
 *
 * @author Thomas Engel
 */
object BarChartConfig extends VisualizationCompanion {

    def name = "BarChart"

    def usage = "BarChart\n" +
                "  Parameters: \n" +
                "    ids:                           Set[String]            - The data identifiers\n" +
                "    height:                        Int                    - Height of the diagram in px\n" +
                "    width:                         Int                    - Width of the diagram in px\n" +
                "    border:                        Border      (optional) - Border (top, bottom, left, right) in px\n" +
                "    colorScheme:                   String      (optional) - The color scheme\n" +
                "    title:                         Title       (optional) - Diagram title (title, position, verticalOffset, horizontalOffset, size, fontFamily, alignment)\n" +
                "    xAxis:                         Axis        (optional) - The x-axis (label, labelSize, labelFontFamily, arrowSize, arrowFilled)\n" +
                "    yAxis:                         ValueAxis   (optional) - The y-axis (label, labelSize, labelFontFamily, min, max, minDistance, unit, unitLabelSize, unitLabelFontFamily, showGrid, showLabels, arrowSize, arrowFilled)\n" +
                "    widthBar:                      Double      (optional) - The width of a bar\n" +
                "    distanceBetweenBars:           Double      (optional) - The distance between two bars\n" +
                "    showValues:                    Boolean     (optional) - If the values for a bar should be shown\n" +
                "    descriptionLabelSize:          Int         (optional) - The font size of the description label\n" +
                "    descriptionLabelFontFamily:    String      (optional) - The font family of the description label\n" +
                "    valueLabelSize:                Int         (optional) - The font size of the value label\n" +
                "    valueLabelFontFamily:          String      (optional) - The font family of the value label"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    def default(ids: Seq[Identifier], height: Int, width: Int) = BarChartConfig(ids,height, width)
    /**
     *  Public constructor that parses JSON into a configuration
     *  @param jsonString representation of the config
     *  @return BarChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): BarChartConfig = {
        implicit val barChartConfigFormat = jsonFormat(BarChartConfig.apply,
            "ids",
            "height", "width",
            "border",
            "colorScheme", "title",
            "xAxis", "yAxis",
            "widthBar", "distanceBetweenBars", "showValues",
            "descriptionLabelSize", "descriptionLabelFontFamily", "valueLabelSize", "valueLabelFontFamily")
        jsonString.parseJson.convertTo[BarChartConfig]
    }

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = Some(List(Aggregation))
        )))


    /**
     * @param config the bar chart config
     * @param data the data
     *
     * @author Thomas Engel, Thomas Weber
     */
    case class BarChart(config: BarChartConfig, data: ProcessedData*) extends Visualization(data: _*) {

        val displayData = data.headOption.getOrElse(throw new IllegalArgumentException("There are no data to display."))
        /*
        val displayData = new ProcessedData("aggregatedData", List(
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(10), Set(new AggregationTag("Wert 2"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))
        */

        // TODO Performance optimization, get both in one run
        val dataMinMaxY: (Double, Double) =
            (displayData.dataPoints.map({ p => p.value.value }).min,
                displayData.dataPoints.map({ p => p.value.value }).max)
        val dataMinY: Double = dataMinMaxY._1
        val dataMaxY: Double = dataMinMaxY._2

        val _yMin = config._yAxis.map(_.min).getOrElse(None)
        val _yMax = config._yAxis.map(_.max).getOrElse(None)
        val _yUnit = config._yAxis.map(_.unit).getOrElse(None)

        // baseline of a bar chart should be 0
        val yScale = _yUnit.map(
            ValueScale(_yMin.getOrElse(math.min(0, dataMinY)),
                _yMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
            ValueScale(_yMin.getOrElse(math.min(0, dataMinY)),
                _yMax.getOrElse(dataMaxY), 0, config.chartHeight)
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

        /**
         * @return the origin
         */
        private def calculateOrigin: (Int, Int) = (config.border.left, config.height - config.border.bottom)

        /**
         * @return a tuple containing the width of a bar and the distance between two bars
         */
        private def calculateWidthBarAndDistanceBetweenBars: (Double, Double) = {
            val size = displayData.dataPoints.size
            val widthBar = config._widthBar
            val distanceBetweenBars = config._distanceBetweenBars

            if(!widthBar.isDefined && !distanceBetweenBars.isDefined) {
                val w = (1.0*config.chartWidth) / (2 * size + 1)
                (w, w)
            } else if(!widthBar.isDefined) {
                ((1.0*(config.chartWidth - (size + 1) * distanceBetweenBars.get)) / (size), distanceBetweenBars.get)
            } else if(!distanceBetweenBars.isDefined) {
                (widthBar.get, (1.0*(config.chartWidth - size*widthBar.get)) / (size + 1))
            } else {
                (widthBar.get, distanceBetweenBars.get)
            }
        }

        /**
         * @return the calculated list of [[BarElement]]s.
         */
        private def calculateBarElements: List[BarElement] = {
            val (widthBar, distanceBetweenBars) = calculateWidthBarAndDistanceBetweenBars
            var leftOffset = distanceBetweenBars

            val result = for(point <- displayData.dataPoints) yield {
                val description = point.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString
                val color = config.colorScheme.next

                var element = new BarElement(
                    identifier = description,
                    xLeft = leftOffset,
                    width = widthBar,
                    height = yScale(point.value.value),
                    description = description,
                    descriptionLabelSize = config.descriptionLabelSize,
                    descriptionLabelFontFamily = config.descriptionLabelFontFamily,
                    classes = point.tags.map(_.toString),
                    showValues = config.showValues,
                    value = point.value.toString,
                    valueLabelSize = config.valueLabelSize,
                    valueLabelFontFamily = config.valueLabelFontFamily,
                    color = color
                )

                leftOffset += widthBar + distanceBetweenBars

                element
            }

            result.reverse
        }


        def toSVG: Elem = {
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox
            val (xtitle, ytitle) = config.getTitleCoordinates
            val chartOrigin = calculateOrigin

            (<g class="bars" transform={ s"translate$chartOrigin" }>
                {for (bar <- calculateBarElements) yield
                    bar.getSVGElement
                }
            </g>: SVGXML)
                .withGrid(Grid(
                    chartOrigin._1,
                    chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xPositions = Seq(),
                    yPositions = if(config.yAxis.showGrid) yAxisLabels.map(_._2) else Seq()))
                .withAxis(HorizontalAxis(
                    x = chartOrigin._1,
                    y = chartOrigin._2,
                    width = config.chartWidth,
                    title = config.xAxis.label,
                    titleSize = config.xAxis.labelSize,
                    titleFontFamily = config.xAxis.labelFontFamily,
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
 * Config for a [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]].
 *  Acts as a factory.
 *
 * @param ids ids set of ids which are to be displayed in the visualization
 * @param height the height
 * @param width the width
 * @param _border the border
 * @param _colorScheme the color scheme
 * @param _title the title
 * @param _xAxis the x-axis
 * @param _yAxis the y-axis
 * @param _widthBar the width of a bar
 * @param _distanceBetweenBars the distance between two bars
 * @param _showValues iff the values for a bar should be shown
 * @param _descriptionLabelSize the font size of the description label
 * @param _descriptionLabelFontFamily the font family of the description label
 * @param _valueLabelSize the font size of the value label
 * @param _valueLabelFontFamily the font family of the value label
 */
case class BarChartConfig(
    val ids:                            Seq[Identifier],
    val height:                         Int,
    val width:                          Int,
    val _border:                        Option[OptBorder]  = None,
    val _colorScheme:                   Option[ColorScheme]         = None,
    val _title:                         Option[OptTitleParam]       = None,
    val _xAxis:                         Option[OptValueAxisParam]   = None,
    val _yAxis:                         Option[OptValueAxisParam]   = None,

    val _widthBar:                      Option[Double]              = None,
    val _distanceBetweenBars:           Option[Double]              = None,
    val _showValues:                    Option[Boolean]             = None,
    val _descriptionLabelSize:          Option[Int]                 = None,
    val _descriptionLabelFontFamily:    Option[String]              = None,
    val _valueLabelSize:                Option[Int]                 = None,
    val _valueLabelFontFamily:          Option[String]              = None
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

    override val X_AXIS_GRID_DEFAULT = false
    override val X_AXIS_LABELS_DEFAULT = false

    /**
     * @return showValues or false
     */
    def showValues: Boolean = _showValues.getOrElse(false)

    /**
     * @return the font size of the description label
     */
    def descriptionLabelSize: Int = _descriptionLabelSize.getOrElse(12)

    /**
     * @return the font family of the description label
     */
    def descriptionLabelFontFamily: String = _descriptionLabelFontFamily.getOrElse("Roboto, Segoe UI")

    /**
     * @return the font size of the value label
     */
    def valueLabelSize: Int = _valueLabelSize.getOrElse(12)

    /**
     * @return the font size of the value label
     */
    def valueLabelFontFamily: String = _valueLabelFontFamily.getOrElse("Roboto, Segoe UI")

    /**
     * Sets the [[Grid]], calculates the list of [[BarElement]]s and returns the
     * [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]]
     * for this configuration.
     *
     * @param pool the pool containing the data
     * @return the [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]] for this configuration
     */
    def apply(pool: DataPool): BarChartConfig.BarChart = {
        BarChartConfig.BarChart(this, pool.getDatasets(ids: _*): _*)
    }

}
