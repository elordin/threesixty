package threesixty.visualizer.visualizations.barChart

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
                "    ids:                   Set[String]            - The data identifiers\n" +
                "    height:                Int                    - Height of the diagram in px\n" +
                "    width:                 Int                    - Width of the diagram in px\n" +
                "    optYMin:               Double      (optional) - Minimum value of the y-axis\n" +
                "    optYMax:               Double      (optional) - Maximum value of the y-axis\n" +
                "    xLabel:                String      (optional) - Label for the x-axis\n" +
                "    yLabel:                String      (optional) - Label for the y-axis\n" +
                "    title:                 String      (optional) - Diagram title\n" +
                "    borderTop:             Int         (optional) - Border to the top in px\n" +
                "    borderBottom:          Int         (optional) - Border to the bottom in px\n" +
                "    borderLeft:            Int         (optional) - Border to the left in px\n" +
                "    borderRight:           Int         (optional) - Border to the right in px\n" +
                "    distanceTitle          Int         (optional) - Distance between the title and the chart in px\n" +
                "    widthBar               Double      (optional) - Width of a bar in px\n" +
                "    distanceBetweenBars    Double      (optional) - Distance between two bars in px\n" +
                "    showValues             Boolean     (optional) - If values should be shown\n" +
                "    minDistanceY           Int         (optional) - Minimum number of px between two control points on the y-axis\n" +
                "    optUnitY               Double      (optional) - Value of the desired unit on the y-axis\n" +
                "    fontSizeTitle          Int         (optional) - Font size of the title\n" +
                "    fontSize               Int         (optional) - Font size of labels\n"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
     *  Public constructor that parses JSON into a configuration
     *  @param jsonString representation of the config
     *  @return BarChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): BarChartConfig = {
        implicit val barChartConfigFormat = jsonFormat(BarChartConfig.apply,
            "ids", "height", "width", "optYMin", "optYMax",
            "xLabel", "yLabel", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "distanceTitle", "widthBar", "distanceBetweenBars", "showValues",
            "minDistanceY", "optUnitY", "fontSizeTitle", "fontSize")
        jsonString.parseJson.convertTo[BarChartConfig]
    }


    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None //TODO Aggregation
        )))


    /**
     * @param config the bar chart config
     * @param data the data
     *
     * @author Thomas Engel, Thomas Weber
     */
    case class BarChart(config: BarChartConfig, data: ProcessedData*) extends Visualization(data: _*) {

        //val displayData = data.headOption.getOrElse(null)
        // TODO: for testing only!!!
        val displayData = new ProcessedData("aggregatedData", List(
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(-10), Set(new AggregationTag("Wert 2"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))

        require(displayData != null, "There are no data to display.")

        // TODO Performance optimization, get both in one run
        val dataMinMaxY: (Double, Double) =
            (displayData.dataPoints.map({ p => p.value.value }).min,
                displayData.dataPoints.map({ p => p.value.value }).max)
        val dataMinY: Double = dataMinMaxY._1
        val dataMaxY: Double = dataMinMaxY._2

        val yScale = config.optUnitY.map(
            ValueScale(config.optYMin.getOrElse(dataMinY),
                config.optYMax.getOrElse(dataMaxY), 0, config.chartHeight, _)).getOrElse {
            ValueScale(config.optYMin.getOrElse(dataMinY),
                config.optYMax.getOrElse(dataMaxY), 0, config.chartHeight)
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

        val chartOrigin = (config.borderLeft, config.height - config.borderBottom)

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
                var description = point.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString

                var element = new BarElement(
                    description.replace(' ', '_'),
                    leftOffset,
                    widthBar,
                    chartOrigin._2 - yScale(point.value.value),
                    description,
                    config.showValues,
                    point.value.value.toString,
                    Some(config.fontSize),
                    Some(DefaultColorScheme.next))

                leftOffset += widthBar + distanceBetweenBars

                result = element :: result
            }

            result.reverse
        }


        def toSVG: Elem = {
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox

            (<g class="bars">
                {for (bar <- calculateBarElements) yield
                    bar.getSVGElement
                }
            </g>: SVGXML)
                .withGrid(Grid(
                    chartOrigin._1,
                    chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    0,
                    yAxisLabels.size))
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
                .withTitle(config.title, config.width / 2, config.borderTop - config.distanceTitle, config.fontSizeTitle)
                .withSVGHeader(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight)
        }
    }
}

/**
 *  Config for a [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]].
 *  Acts as a factory.
 *
 *  @param ids set of ids which are to be displayed in the visualization
 *  @param height the height
 *  @param width the width
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
 *  @param _widthBar the width of a bar
 *  @param _distanceBetweenBars the distance between two bars
 *  @param _showValues iff the values for a bar should be shown
 *  @param _minDistanceY the minimal distance between two grid points on the y-axis
 *  @param optUnitY the unit of the y-axis
 *  @param _fontSizeTitle the font size of the title
 *  @param _fontSize the font size of labels
 *
 * @author Thomas Engel
 */
case class BarChartConfig(
     val ids:                    Seq[Identifier],
     val height:                 Int,
     val width:                  Int,
     val optYMin:                Option[Double] = None,
     val optYMax:                Option[Double] = None,
     val _xLabel:                 Option[String] = None,
     val _yLabel:                 Option[String] = None,
     val _title:                  Option[String] = None,
     val _borderTop:              Option[Int]    = None,
     val _borderBottom:           Option[Int]    = None,
     val _borderLeft:             Option[Int]    = None,
     val _borderRight:            Option[Int]    = None,
     val _distanceTitle:          Option[Int]    = None,
     val _widthBar:               Option[Double] = None,
     val _distanceBetweenBars:    Option[Double] = None,
     val _showValues:             Option[Boolean]= None,
     val _minDistanceY:           Option[Int]    = None,
     val optUnitY:               Option[Double] = None,
     val _fontSizeTitle:          Option[Int]    = None,
     val _fontSize:               Option[Int]    = None
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
    /**
     * @return the label on the x-axis or an empty string
     */
    def xLabel: String = _xLabel.getOrElse("")
    /**
     * @return the label on the y-axis or an empty string
     */
    def yLabel: String = _yLabel.getOrElse("")

    /**
     * @return the minDistanceY or a default value
     */
    def minDistanceY: Int = _minDistanceY.getOrElse(20)
    require(minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

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
