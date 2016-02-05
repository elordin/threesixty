package threesixty.visualizer.visualizations.barChart

import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{Tag, AggregationTag}
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.util._
import spray.json._
import threesixty.visualizer.visualizations.BarElement
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
                "    minDistanceY:              Int         (optional) - The minimum number of px between two grid points on the y-axis\n" +
                "    fontSize:                  Int         (optional) - The font size\n" +
                "    fontFamily:                String      (optional) - The font family\n" +
                "    yMin:                      Double      (optional) - The minimum value displayed on the y-axis\n" +
                "    yMax:                      Double      (optional) - The maximum value displayed on the y-axis\n" +
                "    yUnit:                     Double      (optional) - The unit on the y-axis\n" +
                "    widthBar:                  Double      (optional) - The width of a bar\n" +
                "    distanceBetweenBars:       Double      (optional) - The distance between two bars\n" +
                "    showValues:                Boolean     (optional) - If the values for a bar should be shown"

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
            "colorScheme",
            "title", "titleVerticalOffset", "titleFontSize",
            "xlabel", "ylabel", "minDistanceY",
            "fontSize", "fontFamily",
            "yMin", "yMax", "yUnit",
            "widthBar", "distanceBetweenBars", "showValues")
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
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(-10), Set(new AggregationTag("Wert 2"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))
         */

        // TODO Performance optimization, get both in one run
        val dataMinMaxY: (Double, Double) =
            (displayData.dataPoints.map({ p => p.value.value }).min,
                displayData.dataPoints.map({ p => p.value.value }).max)
        val dataMinY: Double = dataMinMaxY._1
        val dataMaxY: Double = dataMinMaxY._2

        val yScale = config._yUnit.map(
            ValueScale(config._yMin.getOrElse(dataMinY),
                config._yMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
            ValueScale(config._yMin.getOrElse(dataMinY),
                config._yMax.getOrElse(dataMaxY), 0, config.chartHeight)
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
            var result: List[BarElement] = List.empty
            val (widthBar, distanceBetweenBars) = calculateWidthBarAndDistanceBetweenBars
            var leftOffset = distanceBetweenBars

            for(point <- displayData.dataPoints) {
                val description = point.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString
                val color = if(config.colorScheme.isDefined) Some(config.colorScheme.get.next) else None

                var element = new BarElement(
                    identifier = description,
                    xLeft = leftOffset,
                    width = widthBar,
                    height = point.value.value,
                    description = description,
                    classes = point.tags.map(_.toString),
                    showValues = config.showValues,
                    value = point.value.toString,
                    fontSize = config.fontSize,
                    fontFamily = config.fontFamily,
                    color = color)

                leftOffset += widthBar + distanceBetweenBars

                result = element :: result
            }

            result.reverse
        }


        def toSVG: Elem = {
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox
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
                    math.abs(config.chartWidth),
                    math.abs(yScale(yScale.step).toInt)))
                .withAxis(HorizontalAxis(
                    x = chartOrigin._1,
                    y = chartOrigin._2,
                    width = config.chartWidth,
                    title = config.xLabel))
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
 * Config for a [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]].
 *  Acts as a factory.
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
 * @param _minPxBetweenYGridPoints the minimum distance in px between two grid points on the y-axis
 * @param _fontSize the font size of labels
 * @param _fontFamily the font family of labels
 * @param _yMin the minimum value displayed on the y-coordinate
 * @param _yMax the maximum value displayed on the y-coordinate
 * @param _yUnit the unit of the y-axis
 * @param _widthBar the width of a bar
 * @param _distanceBetweenBars the distance between two bars
 * @param _showValues iff the values for a bar should be shown
 */
case class BarChartConfig(
    val ids:                        Seq[Identifier],
    val height:                     Int,
    val width:                      Int,
    val _border:                    Option[Border]  = None,
    val _colorScheme:               Option[String]  = None,
    val _title:                     Option[String]  = None,
    val _titleVerticalOffset:       Option[Int]     = None,
    val _titleFontSize:             Option[Int]     = None,
    val _xLabel:                    Option[String]  = None,
    val _yLabel:                    Option[String]  = None,
    val _minPxBetweenYGridPoints:   Option[Int]     = None,
    val _fontSize:                  Option[Int]     = None,
    val _fontFamily:                Option[String]  = None,

    val _yMin:                      Option[Double] = None,
    val _yMax:                      Option[Double] = None,
    val _yUnit:                     Option[Double] = None,
    val _widthBar:                  Option[Double] = None,
    val _distanceBetweenBars:       Option[Double] = None,
    val _showValues:                Option[Boolean]= None
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
    _minPxBetweenYGridPoints = _minPxBetweenYGridPoints,
    _fontSize = _fontSize,
    _fontFamily = _fontFamily
) {

    /**
     * @return showValues or false
     */
    def showValues: Boolean = _showValues.getOrElse(false)

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
