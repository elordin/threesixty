package threesixty.visualizer.visualizations.scatterChart

import spray.json._
import threesixty.data.Data.Identifier
import threesixty.data.DataJsonProtocol._
import threesixty.data.metadata.Scaling
import threesixty.data.{DataPool, ProcessedData, TaggedDataPoint}
import threesixty.visualizer._
import threesixty.visualizer.util.{Axis, Grid}

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
        implicit val lineChartConfigFormat = jsonFormat21(ScatterChartConfig.apply)
        jsonString.parseJson.convertTo[ScatterChartConfig]
    }


    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            scaling = Some(Scaling.Ordinal)
        ), DataRequirement(
            scaling = Some(Scaling.Ordinal)
        )))


    case class ScatterChart(config: ScatterChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {

        def toSVG: Elem = ???
        /*
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
        */
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
     val optUnitX:     Option[Double]    = None,
     val optUnitY:     Option[Double]    = None,
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

    def xLabel: String = _xLabel.getOrElse("")
    def yLabel: String = _yLabel.getOrElse("")

    def minDistanceX: Int = _minDistanceX.getOrElse(20)
    def minDistanceY: Int = _minDistanceY.getOrElse(20)

    require(minDistanceX > 0, "Value for minDistanceX must be greater than 0.")
    require(minDistanceY > 0, "Value for minDistanceY must be greater than 0.")

    /*
    var grid: Grid = null

    def getGrid: Grid = {
        grid
    }
    */

    /*
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
