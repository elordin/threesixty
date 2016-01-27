package threesixty.visualizer.visualizations.barChart

import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{Tag, AggregationTag}
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.visualizations.general._
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


    case class BarChart(config: BarChartConfig, data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
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
    }
}


case class BarChartConfig(
     val ids:                    Set[Identifier],
     val height:                 Int,
     val width:                  Int,
     val optYMin:                Option[Double] = None,
     val optYMax:                Option[Double] = None,
     val xLabel:                 Option[String] = None,
     val yLabel:                 Option[String] = None,
     val title:                  Option[String] = None,
     val borderTop:              Option[Int]    = None,
     val borderBottom:           Option[Int]    = None,
     val borderLeft:             Option[Int]    = None,
     val borderRight:            Option[Int]    = None,
     val distanceTitle:          Option[Int]    = None,
     val widthBar:               Option[Double] = None,
     val distanceBetweenBars:    Option[Double] = None,
     val showValues:             Option[Boolean]= None,
     val minDistanceY:           Option[Int]    = None,
     val optUnitY:               Option[Double] = None,
     val fontSizeTitle:          Option[Int]    = None,
     val fontSize:               Option[Int]    = None
) extends VisualizationConfig(
    ids,
    height,
    width,
    title,
    borderTop,
    borderBottom,
    borderLeft,
    borderRight,
    distanceTitle,
    fontSizeTitle,
    fontSize) {

    // TODO: for testing only!!!
    val dataTest = new ProcessedData("aggregatedData", List(
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(-10), Set(new AggregationTag("Wert 2"))),
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
        new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))

    def _xLabel: String = xLabel.getOrElse("")
    def _yLabel: String = yLabel.getOrElse("")

    def _minDistanceY: Int = minDistanceY.getOrElse(20)
    require(_minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    def _showValues: Boolean = showValues.getOrElse(false)

    var barElements: List[BarElement] = List.empty
    var grid: Grid = null

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None //TODO Aggregation
        )))

    override def calculateOrigin: (Double, Double) = {
        (_borderLeft, _borderTop - grid.yAxis.convert(grid.yAxis.getMaximumDisplayedValue))
    }

    private def calculateWidthBar(data: ProcessedData): Double = {
        (1.0*widthChart) / (2 * data.dataPoints.size + 1)
    }

    private def calculateDistanceBetweenBars(data: ProcessedData): Double = {
        (1.0*widthChart) / (2 * data.dataPoints.size + 1)
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
                _showValues,
                point.value.value.toString,
                Some(_fontSize),
                Some(ColorScheme.next))

            leftOffset += widthBar + distanceBetweenBars

            barElements = element :: barElements
        }

        barElements = barElements.reverse
    }

    def getBarElements: List[BarElement] = barElements

    def getGrid: Grid = grid

    def apply(pool: DataPool): BarChartConfig.BarChart = {
        //val dataset = pool.getDatasets(ids)
        val dataset = Set(dataTest)

        val _widthBar = widthBar.getOrElse(calculateWidthBar(dataset.head))
        val _distanceBetweenBars = distanceBetweenBars.getOrElse(calculateDistanceBetweenBars(dataset.head))

        require(_widthBar > 0, "Value for widthBar must be greater than 0.")
        require(_distanceBetweenBars >= 0, "Negative value for distanceBetweenBars is not allowed.")

        require(_widthBar*dataset.head.dataPoints.size + _distanceBetweenBars*dataset.head.dataPoints.size <= widthChart,
            "widthBar or distanceBetweenBars is to large to fit into the chart.")

        val datapoints = dataset.head.dataPoints.map((p: TaggedDataPoint) => p.value.value)
        var yMin = optYMin.getOrElse(datapoints.min)
        var yMax = optYMax.getOrElse(datapoints.max)

        yMin = math.min(0, yMin)
        yMax = math.max(0, yMax)

        val xAxis = AxisFactory.createAxis(AxisType.Nothing, AxisDimension.xAxis, widthChart, 0, 0, _xLabel)
        val yAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.yAxis, heightChart, yMin, yMax, _yLabel, Some(_minDistanceY),
            if(optUnitY.isDefined) Some(optUnitY.get.toString) else None)

        grid = new Grid(xAxis, yAxis, _fontSize)

        calculateBarElements(dataset.head, _distanceBetweenBars, _widthBar)

        BarChartConfig.BarChart(this, dataset)
    }

}
