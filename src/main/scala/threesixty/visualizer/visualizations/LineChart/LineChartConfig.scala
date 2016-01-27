package threesixty.visualizer.visualizations.lineChart

import threesixty.data.Data.{Identifier, Timestamp, ValueType}
import threesixty.data.DataJsonProtocol._
import threesixty.data.metadata.{Scaling, Timeframe}
import threesixty.data.{ProcessedData, TaggedDataPoint, DataPool}
import threesixty.visualizer.visualizations.general._
import threesixty.visualizer.{DataRequirement, Visualization, VisualizationCompanion, VisualizationConfig, VisualizationMetadata, VisualizationMixins}

import scala.xml.Elem

import spray.json._


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("linechart" -> LineChartConfig)
}


object LineChartConfig extends VisualizationCompanion {

    def name = "LineChart"

    def usage = "LineChart\n" +
                "  Parameters: \n" +
                "    height:            Int                  - Height of the diagram in px\n" +
                "    width:             Int                  - Width of the diagram in px\n" +
                "    optXMin:           Timestamp (optional) - Minimum value of the x-axis\n" +
                "    optXMax:           Timestamp (optinmal) - Maximum value of the x-axis\n" +
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
                "    optUnitX           String    (optional) - Name of the desired unit on the x-axis\n" +
                "    optUnitY           Double    (optional) - Value of the desired unit on the y-axis\n" +
                "    fontSizeTitle      Int       (optional) - Font size of the title\n" +
                "    fontSize           Int       (optional) - Font size of labels\n"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }


    /**
     *  Public constructor that parses JSON into a LineChartConfig
     *  @param json representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): LineChartConfig = {
        implicit val lineChartConfigFormat = jsonFormat(LineChartConfig.apply,
            "ids", "height", "width", "optXMin", "optXMax", "optYMin", "optYMax",
            "xLabel", "yLabel", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "distanceTitle", "minDistanceX", "minDistanceY", "optUnitX", "optUnitY", "fontSizeTitle", "fontSize")
        jsonString.parseJson.convertTo[LineChartConfig]
    }

    case class LineChart(config: LineChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        private def calculatePath(data: ProcessedData): String = {
            val grid = config.getGrid

            var path = ""

            for {d <- data.dataPoints} {
                val (x, y) = grid.convertPoint(d.timestamp.getTime, d.value.value)

                if (path.isEmpty) {
                    path += "M "
                } else {
                    path += " L "
                }
                path += x + " " + y
            }

            path
        }

        def getSVGElements: List[Elem] = {
            var grid = config.getGrid

            List(
                grid.getSVGElement,
                <g id="data">
                    {for {dataset <- data} yield
                    <g id={dataset.id}>
                        <g id="datapoints">
                            {for (datapoint <- dataset.dataPoints) yield
                                <circle fill="#00008B"
                                        stroke="#00008B"
                                        cx={grid.xAxis.convert(datapoint.timestamp.getTime).toString}
                                        cy={grid.yAxis.convert(datapoint.value.value).toString}
                                        r="4" />
                            }
                        </g>
                        <path stroke="#6495ED"
                              fill="none"
                              stroke-width="2"
                              d={calculatePath(dataset)}/>
                    </g>}
                </g>
            )
        }
    }
}


case class LineChartConfig(
    val ids: Set[Identifier],
    val height: Int,
    val width: Int,
    val optXMin:      Option[Timestamp] = None,
    val optXMax:      Option[Timestamp] = None,
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
    val optUnitX:     Option[String]    = None,
    val optUnitY:     Option[Double]    = None,
    val fontSizeTitle:Option[Int]       = None,
    val fontSize:     Option[Int]       = None
) extends VisualizationConfig(
    ids: Set[Identifier],
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

    require(_minDistanceX > 0, "Value for minDistanceX must be positive.")
    require(_minDistanceY > 0, "Value for minDistanceY must be positive.")

    val metadata = new VisualizationMetadata(
            List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    var yMin: Double = 0
    var yMax: Double = 0

    var xMin: Long = 0
    var xMax: Long = 0

    var grid: Grid = null

    def getGrid: Grid = {
        grid
    }

    private def calculateXMinMax(pool: DataPool): (Double, Double) = {
        if (!optXMin.isDefined || !optXMax.isDefined) {
            val xframe = Timeframe.deduceProcessedData(pool.getDatasets(ids))
            xMin = if (optXMin.isDefined) math.min(xframe.start.getTime, math.max(0, optXMin.get.getTime)) else xframe.start.getTime
            xMax = if (optXMax.isDefined) math.max(xframe.end.getTime, optXMax.get.getTime) else xframe.end.getTime
        } else {
            xMin = optXMin.get.getTime
            xMax = optXMax.get.getTime
        }

        (xMin, xMax)
    }

    private def calculateYMin(data: Iterable[Double]): Double = {
        data.reduceLeft((l,r) => if (l < r) l else r)
    }

    private def calculateYMinMulti(data: Iterable[ProcessedData]): ValueType = {
        val mins = data.map((d: ProcessedData) => calculateYMin(d.dataPoints.map((x: TaggedDataPoint) => x.value.value)))
        calculateYMin(mins)
    }

    private def calculateYMax(data: Iterable[Double]): Double = {
        data.reduceLeft((l,r) => if (l > r) l else r)
    }

    private def calculateYMaxMulti(data: Iterable[ProcessedData]): ValueType = {
        val maxs = data.map((d: ProcessedData) => calculateYMax(d.dataPoints.map((x: TaggedDataPoint) => x.value.value)))
        calculateYMax(maxs)
    }

    override def calculateOrigin: (Double, Double) = {
        (_borderLeft, _borderTop - math.ceil(grid.yAxis.convert(grid.yAxis.getMaximumDisplayedValue)).toInt)
    }

    def apply(pool: DataPool): LineChartConfig.LineChart = {
        val (min, max) = calculateXMinMax(pool)
        xMin = min.toLong
        xMax = max.toLong

        yMin = optYMin.getOrElse(calculateYMinMulti(pool.getDatasets(ids)).value)
        yMax = optYMax.getOrElse(calculateYMaxMulti(pool.getDatasets(ids)).value)

        val xAxis = AxisFactory.createAxis(AxisType.TimeAxis, AxisDimension.xAxis, widthChart, xMin, xMax, _xLabel, Some(_minDistanceX), optUnitX)
        val yAxis = AxisFactory.createAxis(AxisType.ValueAxis, AxisDimension.yAxis, heightChart, yMin, yMax, _yLabel, Some(_minDistanceY),
            if(optUnitY.isDefined) Some(optUnitY.get.toString) else None)

        grid = new Grid(xAxis, yAxis, _fontSize)

        LineChartConfig.LineChart(this, pool.getDatasets(ids))
    }

}
