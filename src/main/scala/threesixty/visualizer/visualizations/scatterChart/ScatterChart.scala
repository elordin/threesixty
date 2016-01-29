package threesixty.visualizer.visualizations.scatterChart

import spray.json._
import threesixty.data.Data.Identifier
import threesixty.data.DataJsonProtocol._
import threesixty.data.metadata.Scaling
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.visualizations.general.{AxisDimension, AxisFactory, AxisType, Grid}

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("scatterchart" -> ScatterChartConfig)
}

/**
  * The config class for a [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]].
  *
  * @author Thomas Engel
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
        implicit val lineChartConfigFormat = jsonFormat(ScatterChartConfig.apply,
            "ids", "height", "width", "optXMin", "optXMax", "optYMin", "optYMax",
            "xLabel", "yLabel", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "distanceTitle", "minDistanceX", "minDistanceY", "optUnitX", "optUnitY", "fontSizeTitle", "fontSize")
        jsonString.parseJson.convertTo[ScatterChartConfig]
    }

    /**
      * This class creates the svg element for a scatter chart.
      *
      * @param config the scatter chart config
      * @param data the data
      *
      * @author Thomas Engel
      */
    case class ScatterChart(config: ScatterChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        /**
          * @return a list of svg elements that should be included into the chart
          */
        def getSVGElements: List[Elem] = {
            val xdata = data.head
            val ydata = data.last

            val zippedData = xdata.dataPoints.zip(ydata.dataPoints)
            val grid = config.getGrid

            List(
                grid.getSVGElement,
                <g id="datapoints">
                    {for(d <- zippedData) yield
                        <circle cx={grid.xAxis.convert(d._1.value.value).toString}
                                cy={grid.yAxis.convert(d._2.value.value).toString}
                                fill="#00008B"
                                r="2" />
                    }
                </g>
            )
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
  * @param xLabel the label on the x-axis
  * @param yLabel the label on the y-axis
  * @param title the title
  * @param borderTop the border to the top
  * @param borderBottom the border to the bottom
  * @param borderLeft the border to the left
  * @param borderRight the border to the right
  * @param distanceTitle the distance between the title and the top of the chart
  * @param minDistanceX the minimal distance between two grid points on the x-axis
  * @param minDistanceY the minimal distance between two grid points on the y-axis
  * @param optUnitX the unit of the x-axis
  * @param optUnitY the unit of the y-axis
  * @param fontSizeTitle the font size of the title
  * @param fontSize the font size of labels
  */
case class ScatterChartConfig(
     val ids:          Set[Identifier],
     val height:       Int,
     val width:        Int,
     val optXMin:      Option[Double]    = None,
     val optXMax:      Option[Double]    = None,
     val optYMin:      Option[Double]    = None,
     val optYMax:      Option[Double]    = None,
     val xLabel:       Option[String]    = None,
     val yLabel:       Option[String]    = None,
     val title:        Option[String]    = None,
     val borderTop:    Option[Int]       = None,
     val borderBottom: Option[Int]       = None,
     val borderLeft:   Option[Int]       = None,
     val borderRight:  Option[Int]       = None,
     val distanceTitle:Option[Int]       = None,
     val minDistanceX: Option[Int]       = None,
     val minDistanceY: Option[Int]       = None,
     val optUnitX:     Option[Double]    = None,
     val optUnitY:     Option[Double]    = None,
     val fontSizeTitle:Option[Int]       = None,
     val fontSize:     Option[Int]       = None
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

    /**
      * @return the label on the x-axis or an empty string
      */
    def _xLabel: String = xLabel.getOrElse("")

    /**
      * @return the label on the y-axis or an empty string
      */
    def _yLabel: String = yLabel.getOrElse("")

    /**
      * @return the minDistanceX or a default value
      */
    def _minDistanceX: Int = minDistanceX.getOrElse(20)
    /**
      * @return the minDistanceY or a default value
      */
    def _minDistanceY: Int = minDistanceY.getOrElse(20)

    require(_minDistanceX > 0, "Value for minDistanceX must be greater than 0.")
    require(_minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

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

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal)
        )))

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
        (_borderLeft - grid.xAxis.convert(grid.xAxis.getMinimumDisplayedValue),
            _borderTop - grid.yAxis.convert(grid.yAxis.getMaximumDisplayedValue))
    }

    /**
      * Sets the [[Grid]] and returns the
      * [[threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig.ScatterChart]]
      * for this configuration.
      *
      * @param pool the pool containing the data
      * @return the [[threesixty.visualizer.visualizations.lineChart.LineChartConfig.LineChart]] for this configuration
      */
    def apply(pool: DataPool): ScatterChartConfig.ScatterChart =  {
        val dataset = pool.getDatasets(ids)
        val xdata = dataset.head
        val ydata = dataset.last

        val (xMin, xMax) = calculateMinMax(xdata, optXMin, optXMax)
        val (yMin, yMax) = calculateMinMax(ydata, optYMin, optYMax)

        val xAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.xAxis, widthChart, xMin, xMax, _xLabel,
            Some(_minDistanceX), if(optUnitX.isDefined) Some(optUnitX.get.toString) else None)
        val yAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.yAxis, heightChart, yMin, yMax, _yLabel,
            Some(_minDistanceY), if(optUnitY.isDefined) Some(optUnitY.get.toString) else None)

        grid = new Grid(xAxis, yAxis, _fontSize)

        ScatterChartConfig.ScatterChart(this, pool.getDatasets(ids))
    }

}
