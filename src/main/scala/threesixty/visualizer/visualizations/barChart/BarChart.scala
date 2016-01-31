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


    /**
      * This class creates the svg element for a bar chart.
      *
      * @param config the bar chart config
      * @param data the data
      *
      * @author Thomas Engel
      */
    case class BarChart(config: BarChartConfig, data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        /**
          * @return a list of svg elements that should be included into the chart
          */
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

/**
  * The config to create a [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]].
  *
  * @param ids set of ids which are to be displayed in the visualization
  * @param height the height
  * @param width the width
  * @param optYMin the minimum value displayed on the y-coordinate
  * @param optYMax the maximum value displayed on the y-coordinate
  * @param xLabel the label on the x-axis
  * @param yLabel the label on the y-axis
  * @param title the title
  * @param borderTop the border to the top
  * @param borderBottom the border to the bottom
  * @param borderLeft the border to the left
  * @param borderRight the border to the right
  * @param distanceTitle the distance between the title and the top of the chart
  * @param widthBar the width of a bar
  * @param distanceBetweenBars the distance between two bars
  * @param showValues iff the values for a bar should be shown
  * @param minDistanceY the minimal distance between two grid points on the y-axis
  * @param optUnitY the unit of the y-axis
  * @param fontSizeTitle the font size of the title
  * @param fontSize the font size of labels
  *
  * @author Thomas Engel
  */
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

    /**
      * @return the label on the x-axis or an empty string
      */
    def _xLabel: String = xLabel.getOrElse("")

    /**
      * @return the label on the y-axis or an empty string
      */
    def _yLabel: String = yLabel.getOrElse("")

    /**
      * @return the minDistanceY or a default value
      */
    def _minDistanceY: Int = minDistanceY.getOrElse(20)
    require(_minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    /**
      * @return showValues or false
      */
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

    /**
      * @param data the data
      * @return a tuple containing the width of a bar and the distance between two bars
      */
    private def calculateDistances(data: ProcessedData) = {
        if(!widthBar.isDefined && !distanceBetweenBars.isDefined) {
            val w = (1.0*widthChart) / (2 * data.dataPoints.size + 1)
            (w, w)
        } else if(!widthBar.isDefined) {
            ((1.0*(widthChart - (data.dataPoints.size + 1) * distanceBetweenBars.get)) / (data.dataPoints.size), distanceBetweenBars.get)
        } else if(!distanceBetweenBars.isDefined) {
            (widthBar.get, (1.0*(widthChart - data.dataPoints.size*widthBar.get)) / (data.dataPoints.size + 1))
        } else {
            (widthBar.get, distanceBetweenBars.get)
        }
    }

    /**
      * Calculates the list of [[BarElement]] and assigns them to the corresponding variable.
      *
      * @param data the data
      * @param distanceBetweenBars the distance between two bars
      * @param widthBar the width of a bar
      */
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

    /**
      * Please note: The method [[calculateBarElements()]] has to be invoked previously to get
      * the proper list of [[BarElement]]s. Otherwise an empty list will be returned.
      *
      * @return the list of [[BarElement]]s
      */
    def getBarElements: List[BarElement] = barElements

    /**
      * Please note: The grid has to be set before in order to get the proper grid.
      * The grid will only be set in the method [[apply()]].
      *
      * @return the [[Grid]]
      */
    def getGrid: Grid = grid

    /**
      * Sets the [[Grid]], calculates the list of [[BarElement]]s and returns the
      * [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]]
      * for this configuration.
      *
      * @param pool the pool containing the data
      * @return the [[threesixty.visualizer.visualizations.barChart.BarChartConfig.BarChart]] for this configuration
      */
    def apply(pool: DataPool): BarChartConfig.BarChart = {
        //val dataset = pool.getDatasets(ids)
        val dataset = Set(dataTest)

        val (_widthBar, _distanceBetweenBars) = calculateDistances(dataset.head)

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
