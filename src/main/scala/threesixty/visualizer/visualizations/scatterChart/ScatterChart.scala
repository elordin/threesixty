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
                "    ids:               Set[String]          - The data identifiers\n" +
                "    height:            Int                  - Height of the diagram in px\n" +
                "    width:             Int                  - Width of the diagram in px\n" +
                "    optXMin:           Double    (optional) - Minimum value of the x-axis\n" +
                "    optXMax:           Double    (optinmal) - Maximum value of the x-axis\n" +
                "    optYMin:           Double    (optional) - Minimum value of the y-axis\n" +
                "    optYMax:           Double    (optional) - Maximum value of the y-axis\n" +
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
                "    optUnitX           Double    (optional) - Value of the desired unit on the x-axis\n" +
                "    optUnitY           Double    (optional) - Value of the desired unit on the y-axis\n" +
                "    fontSizeTitle      Int       (optional) - Font size of the title\n" +
                "    fontSize           Int       (optional) - Font size of labels\n"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
     *  Public constructor that parses JSON into a configuration
     *  @param jsonString representation of the config
     *  @return ScatterChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): ScatterChartConfig = {
        implicit val lineChartConfigFormat = jsonFormat21(ScatterChartConfig.apply)
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
     * @author Thomas Engel
     */
    case class ScatterChart(config: ScatterChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
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

            /*
            val xdata = data.head
            val ydata = data.last

            val zippedData = xdata.dataPoints.zip(ydata.dataPoints)
            */
            (<g id="datapoints">
                { for { dataset <- data } yield  {
                    val color = ColorScheme.next
                    for { datapoint <- dataset.dataPoints } yield
                        <circle
                            cx={ (chartOrigin._1 + xScale(datapoint.timestamp.getTime)).toString }
                            cy={ (chartOrigin._2 - yScale(datapoint.value.value)).toString }
                            fill={ color.toHexString }
                            r="2" />
                } }
            </g>: SVGXML)
                .withGrid(Grid(
                    chartOrigin._1,
                    chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xAxisLabels.size,
                    yAxisLabels.size,
                    xOffset = xScale(xScale.nextBreakpoint(dataMinX)),
                    yOffset = yScale(yScale.nextBreakpoint(dataMinY))
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
                .withTitle(config.title, config.width / 2, config.borderTop - config.distanceTitle, config.fontSizeTitle)
                .withSVGHeader(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight)

        }
    }
}


/**
  * The config to create a [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]].
  *
  * @param ids set of ids which are to be displayed in the visualization
  * @param height the height
  * @param width the width
  * @param optXMin the minimum value displayed on the x-coordinate
  * @param optXMax the maximum value displayed on the x-coordinate
  * @param optYMin the minimum value displayed on the y-coordinate
  * @param optYMax the maximum value displayed on the y-coordinate
  * @param _xLabel the label on the x-axis
  * @param _yLabel the label on the y-axis
  * @param _title the title
  * @param _borderTop the border to the top
  * @param _borderBottom the border to the bottom
  * @param _borderLeft the border to the left
  * @param _borderRight the border to the right
  * @param _minDistanceX the minimal distance between two grid points on the x-axis
  * @param _minDistanceY the minimal distance between two grid points on the y-axis
  * @param optUnitX the unit of the x-axis
  * @param optUnitY the unit of the y-axis
  * @param _fontSizeTitle the font size of the title
  * @param _fontSize the font size of labels
  */
case class ScatterChartConfig(
     val ids:          Set[Identifier],
     val height:       Int,
     val width:        Int,
     val optXMin:      Option[Timestamp]    = None,
     val optXMax:      Option[Timestamp]    = None,
     val optYMin:      Option[Double]    = None,
     val optYMax:      Option[Double]    = None,
     val _xLabel:       Option[String]    = None,
     val _yLabel:       Option[String]    = None,
     val _title:        Option[String]    = None,
     val _borderTop:    Option[Int]       = None,
     val _borderBottom: Option[Int]       = None,
     val _borderLeft:   Option[Int]       = None,
     val _borderRight:  Option[Int]       = None,
     val _distanceTitle:Option[Int]       = None,
     val _minDistanceX: Option[Int]       = None,
     val _minDistanceY: Option[Int]       = None,
     val optUnitX:      Option[String]    = None,
     val optUnitY:      Option[Double]    = None,
     val _fontSizeTitle:Option[Int]       = None,
     val _fontSize:     Option[Int]       = None
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
    _fontSize) {

    /**
     * @return the label on the x-axis or an empty string
     */
    def xLabel: String = _xLabel.getOrElse("")

    /**
     * @return the label on the y-axis or an empty string
     */
    def yLabel: String = _yLabel.getOrElse("")

    /**
     * @return the minDistanceX or a default value
     */
    def minDistanceX: Int = _minDistanceX.getOrElse(20)
    /**
      * @return the minDistanceY or a default value
      */
    def minDistanceY: Int = _minDistanceY.getOrElse(20)

    require(minDistanceX > 0, "Value for minDistanceX must be greater than 0.")
    require(minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    /*
    var grid: Grid = null

    /**
     * Please note: The grid has to be set before in order to get the proper grid.
     * The grid will only be set in the method [[apply()]].
     *
     * @return the [[Grid]]
     */
    def getGrid: Grid = {
        grid
    }
    */

    /*

    /**
     * Completes the given optional minimum and maximum displayed value if needed.
     *
     * @param data the data
     * @param minimum the optional minimum value
     * @param maximum the optional maximum value
     * @return a tuple containing the minimum and maximum value for the given data
     */
    private def calculateMinMax(data: ProcessedData, minimum: Option[Double], maximum: Option[Double]) = {
        var datapoints: List[Double] = List.empty
        if(!minimum.isDefined || !maximum.isDefined) {
            // only calculate if needed because of performance issue
            datapoints = data.dataPoints.map((p: TaggedDataPoint) => p.value.value)
        }

        val min = optYMin.getOrElse(datapoints.min)
        val max = optYMax.getOrElse(datapoints.max)

        (min, max)
    }

    override def calculateOrigin: (Double, Double) = {
        (borderLeft - grid.xAxis.convert(grid.xAxis.getMinimumDisplayedValue),
            borderTop - grid.yAxis.convert(grid.yAxis.getMaximumDisplayedValue))
    }
    */

    /**
     *  Returns a
     *  [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]]
     *  using visualizing the given dataset with configuration.
     *
     *  @param pool the pool containing the data
     *  @return the [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]] for this configuration
     */
    def apply(pool: DataPool): ScatterChartConfig.ScatterChart =  {

        /*
        val dataset = pool.getDatasets(ids)
        val xdata = dataset.head
        val ydata = dataset.last

        val (xMin, xMax) = calculateMinMax(xdata, optXMin, optXMax)
        val (yMin, yMax) = calculateMinMax(ydata, optYMin, optYMax)

        val xAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.xAxis, chartWidth, xMin, xMax, _xLabel,
            Some(minDistanceX), if(optUnitX.isDefined) Some(optUnitX.get.toString) else None)
        val yAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.yAxis, chartHeight, yMin, yMax, _yLabel,
            Some(minDistanceY), if(optUnitY.isDefined) Some(optUnitY.get.toString) else None)

        grid = new Grid(xAxis, yAxis, _fontSize) */

        ScatterChartConfig.ScatterChart(this, pool.getDatasets(ids))
    }

}
