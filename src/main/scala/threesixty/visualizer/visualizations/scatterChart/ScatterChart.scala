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


    case class ScatterChart(config: ScatterChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
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

    def _xLabel: String = xLabel.getOrElse("")
    def _yLabel: String = yLabel.getOrElse("")

    def _minDistanceX: Int = minDistanceX.getOrElse(20)
    def _minDistanceY: Int = minDistanceY.getOrElse(20)

    require(_minDistanceX > 0, "Value for minDistanceX must be greater than 0.")
    require(_minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    var grid: Grid = null

    def getGrid: Grid = {
        grid
    }

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal)
        )))

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
