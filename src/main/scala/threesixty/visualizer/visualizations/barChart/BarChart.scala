package threesixty.visualizer.visualizations.barChart

import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{Tag, AggregationTag}
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.util._
import spray.json._
import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("barchart" -> BarChartConfig)
}


/**
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


    case class BarChart(config: BarChartConfig, data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {

        val xAxisLabels: Seq[(String, Int)] = ??? // TODO
        val yAxisLabels: Seq[(String, Int)] = ??? // TODO


        def toSVG: Elem = (<g class="bars">
                {
                /*
                    for (bar <- config.getBarElements) yield
                        bar.getSVGElement
                */
                }
            </g>: SVGXML)
                .withGrid(Grid(
                    config.chartOrigin._1,
                    config.chartOrigin._2,
                    config.chartWidth,
                    config.chartHeight,
                    xAxisLabels.size,
                    yAxisLabels.size))
                .withAxis(HorizontalAxis(
                    x = config.chartOrigin._1,
                    y = config.chartOrigin._2,
                    width = config.chartWidth,
                    title = config.xLabel,
                    labels = xAxisLabels))
                .withAxis(VerticalAxis(
                    x = config.chartOrigin._1,
                    y = config.chartOrigin._2,
                    height = config.chartHeight,
                    title = config.yLabel,
                    labels = yAxisLabels))
                .withTitle(config.title, 1, 2, config.fontSizeTitle)
                .withSVGHeader(0, 0, config.width, config.height)

        /*
        def getSVGElements: List[Elem] = {
            List(
                config.getGrid.getSVGElement,
                <g id="bars">
                    {for (bar <- config.getBarElements) yield
                        bar.getSVGElement
                    }
                </g>
            )
        }
        */
    }
}


case class BarChartConfig(
     val ids:                    Set[Identifier],
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
    _fontSize) {

    // TODO: for testing only!!!
    val dataTest = new ProcessedData("aggregatedData", List(
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(-10), Set(new AggregationTag("Wert 2"))),
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))

    def xLabel: String = _xLabel.getOrElse("")
    def yLabel: String = _yLabel.getOrElse("")

    def minDistanceY: Int = _minDistanceY.getOrElse(20)
    require(minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    def showValues: Boolean = _showValues.getOrElse(false)


    /*
    var barElements: List[BarElement] = List.empty
    var grid: Grid = null

    override def calculateOrigin: (Double, Double) = {
        (borderLeft, borderTop - grid.yAxis.convert(grid.yAxis.getMaximumDisplayedValue))
    }

    private def calculateWidthBar(data: ProcessedData): Double = {
        (1.0* chartWidth) / (2 * data.dataPoints.size + 1)
    }

    private def calculateDistanceBetweenBars(data: ProcessedData): Double = {
        (1.0* chartWidth) / (2 * data.dataPoints.size + 1)
    }

    private def calculateBarElements(data: ProcessedData, distanceBetweenBars: Double, widthBar: Double): Unit = {
        var leftOffset = distanceBetweenBars

        for(point <- data.dataPoints) {
            var description = point.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString

            var element = new BarElement(
                description,
                leftOffset,
                widthBar,
                grid.yAxis.convert(point.value.value),
                description,
                showValues,
                point.value.value.toString,
                Some(fontSize),
                Some(ColorScheme.next))

            leftOffset += widthBar + distanceBetweenBars

            barElements = element :: barElements
        }

        barElements = barElements.reverse
    }

    def getBarElements: List[BarElement] = barElements

    def getGrid: Grid = grid

    */
    def apply(pool: DataPool): BarChartConfig.BarChart = {
    /*
        //val dataset = pool.getDatasets(ids)
        val dataset = Set(dataTest)

        val widthBar = _widthBar.getOrElse(calculateWidthBar(dataset.head))
        val distanceBetweenBars = _distanceBetweenBars.getOrElse(calculateDistanceBetweenBars(dataset.head))

        require(widthBar > 0, "Value for widthBar must be greater than 0.")
        require(distanceBetweenBars >= 0, "Negative value for distanceBetweenBars is not allowed.")

        require(widthBar*dataset.head.dataPoints.size + distanceBetweenBars*dataset.head.dataPoints.size <=  chartWidth,
            "widthBar or distanceBetweenBars is to large to fit into the chart.")

        val datapoints = dataset.head.dataPoints.map((p: TaggedDataPoint) => p.value.value)
        var yMin = optYMin.getOrElse(datapoints.min)
        var yMax = optYMax.getOrElse(datapoints.max)

        yMin = math.min(0, yMin)
        yMax = math.max(0, yMax)

        val xAxis = AxisFactory.createAxis(AxisType.Nothing, AxisDimension.xAxis,  chartWidth, 0, 0, xLabel)
        val yAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.yAxis, chartHeight, yMin, yMax, yLabel, Some(minDistanceY),
            if(optUnitY.isDefined) Some(optUnitY.get.toString) else None)

        grid = new Grid(xAxis, yAxis, fontSize)

        calculateBarElements(dataset.head, distanceBetweenBars, widthBar)
    */
        BarChartConfig.BarChart(this, pool.getDatasets(ids))
    }

}
