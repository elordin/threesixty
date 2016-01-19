package threesixty.visualizer.visualizations

import threesixty.data.{TaggedDataPoint, ProcessedData}
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.visualizer.{
        Visualization,
        VisualizationConfig,
        withVisualizationInfos,
        VisualizationInfo,
        DataRequirement,
        VisualizationMetadata
    }
import threesixty.data.metadata.{Timeframe, Scaling}
import threesixty.config.Config
import scala.xml.Elem


object LineChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("linechart" ->
                VisualizationInfo(
                    "LineChart",
                    { json:String => LineChartConfig.apply(json) },
                    "Parameters: \n" +
                    "    height: Int    - Height of the diagram in px\n" +
                    "    width:  Int    - Width of the diagram in px\n" +
                    "    xMin:   Int    - Minimum value of the x-axis\n" +
                    "    xMax:   Int    - Maximum value of the x-axis\n" +
                    "    yMin:   Int    - Minimum value of the y-axis\n" +
                    "    yMax:   Int    - Maximum value of the y-axis\n" +
                    "    xLabel: String - Label for the x-axis\n" +
                    "    yLabel: String - Label for the y-axis\n" +
                    "    title:  String - Diagram title\n"
                )
            )
    }


    /**
     *  Public constructor that parses JSON into a configuration
     *  @param json representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(json: String): LineChartConfig = new LineChartConfig(Set("data1i", "data2i", "data3i"), 768, 1024, title="Test Chart") // TODO actually read JSON


    case class LineChart(config: LineChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: Elem =
            // TODO: Base size on config
            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox={config.calculateViewBoxString()} xml:space="preserve">
                <g id="grid">
                    // background-grid
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="512.5" x2="896" y2="512.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="576.5" x2="896" y2="576.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="448.5" x2="896" y2="448.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="384.5" x2="896" y2="384.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="320.5" x2="896" y2="320.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="256.5" x2="896" y2="256.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="192.5" x2="896" y2="192.5"/>
                </g>
                <line id="X-Axis" fill="none" stroke="#000000" x1="128" y1="640.5" x2="896" y2="640.5"/>
                <line id="Y-Axis" fill="none" stroke="#000000" x1="127.5" y1="641" x2="127.5" y2="129"/>
                <g id="x-ArrowHead">
                    <line fill="none" stroke="#000000" x1="896" y1="640.5" x2="884.687" y2="629.187"/>
                    <line fill="none" stroke="#000000" x1="896" y1="640.5" x2="884.687" y2="651.813"/>
                </g>
                <g id="y-ArrowHead">
                    <line fill="none" stroke="#000000" x1="127.5" y1="129" x2="116.187" y2="140.313"/>
                    <line fill="none" stroke="#000000" x1="127.5" y1="129" x2="138.813" y2="140.313"/>
                </g>
                <g id="yValueIndicators">
                    // TODO: Generate dynamically based on axis range and splitting
                    <line fill="none" stroke="#000000" x1="120" y1="512.5" x2="128" y2="512.5"/>
                    <line fill="none" stroke="#000000" x1="128" y1="576.5" x2="120" y2="576.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="448.5" x2="128" y2="448.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="384.5" x2="128" y2="384.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="320.5" x2="128" y2="320.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="256.5" x2="128" y2="256.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="192.5" x2="128" y2="192.5"/>
                </g>

                // TODO: Generate dynamically based on datapoints
                { for (dataset <- data) yield
                    <g id={dataset.id}>
                        // TODO: Line coordinates, color etc.
                        <polyline fill="none" stroke="#cc0000" stroke-width="2px" points="127.5,500 256,418 384,458 512,378 640,375 768,300 896,325"/>
                        <g id="datapoints">
                            { for (datapoint <- dataset.data) yield
                                <circle fill="#333333" stroke="#000000" cx={ (datapoint.timestamp.getTime * 16 + 128).toString } cy={ (578 - datapoint.value.value * 2).toString } r="4"/>
                            }
                        </g>
                    </g>
                }
                <text transform="matrix(1 0 0 1 468.8481 78.0879)" font-family="Roboto, Segoe UI" font-weight="100" font-size="48">{ config.title }</text>
                <text transform="matrix(1 0 0 1 492.2236 708.6963)" font-family="Roboto, Segoe UI" font-size="16">{ config.xLabel }</text>
                <text transform="matrix(0 -1 1 0 68.6958 403.8643)" font-family="Roboto, Segoe UI" font-size="16">{ config.yLabel }</text>
            </svg>
    }
}


case class LineChartConfig(
    val ids: Set[Identifier],
    val height: Int,
    val width: Int,
    val optXMin: Option[Timestamp] = None,
    val optXMax: Option[Timestamp] = None,
    val optYMin: Option[ValueType] = None,
    val optYMax: Option[ValueType] = None,
    val xLabel: String = "",
    val yLabel: String = "",
    val title: String = "",
    val borderTop: Int = 50,
    val borderBottom: Int = 50,
    val borderLeft: Int = 50,
    val borderRight: Int = 50,
    val minDistanceX: Int = 50,
    val minDistanceY: Int = 50,
    val optUnitY: Option[Double] = None
) extends VisualizationConfig(ids: Set[Identifier]) {
    require(borderTop >= 0, "Negative value for borderTop is not allowed.")
    require(borderBottom >= 0, "Negative value for borderBottom is not allowed.")
    require(borderLeft >= 0, "Negative value for borderLeft is not allowed.")
    require(borderRight >= 0, "Negative value for borderRight is not allowed.")

    require(minDistanceX > 0, "Value for minDistanceX must be positive.")
    require(minDistanceY > 0, "Value for minDistanceY must be positive.")

    // calculate the available height and width for the chart
    val heightChart = height - borderTop - borderBottom
    require(heightChart > 0, "The available height for the chart must be greater than 0.")
    val widthChart = width - borderLeft - borderRight
    require(widthChart > 0, "The available width for the chart must be greater than 0.")

    val metadata = new VisualizationMetadata(
            List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    var xMin: Long = 0
    var xMax: Long = 0
    var unitX: Double = 0
    var stepX: Double = 0
    var amountXPoints: Int = 0

    var yMin: Double = 0
    var yMax: Double = 0
    var unitY: Double = 0
    var stepY: Double = 0
    var amountYPoints: Int = 0

    def preProcessing(config: Config): Unit = {
        // get x/y min/max
        if (!optXMin.isDefined || !optXMax.isDefined) {
            val xframe = Timeframe.deduceProcessedData(config.getDatasets(ids))
            xMin = xframe.start.getTime
            xMax = xframe.end.getTime
        } else {
            xMin = optXMin.get.getTime
            xMax = optXMax.get.getTime
        }

        yMin = optYMin.getOrElse(calculateYMinMulti(config.getDatasets(ids))).value
        yMax = optYMax.getOrElse(calculateYMaxMulti(config.getDatasets(ids))).value

        // calculate the distance between two control points on the y-axis
        unitY = optUnitY.getOrElse(-1.0)
        if(unitY <= 0) {
            unitY = calculateUnitY()
        }
        amountYPoints = math.ceil((yMax - yMin) / unitY).toInt
        stepY = (1.0*heightChart) / amountYPoints

        // calculate yMin and yMax for the min/max displayed value
        yMin = math.ceil(yMin / unitY) * unitY
        yMax = math.ceil(yMax / unitY) * unitY

        // calculate the distance between two control points on the x-axis
        unitX = calculateUnitX()
        amountXPoints = math.ceil((xMax - xMin) / unitX).toInt
        stepX = (1.0*widthChart) / amountXPoints

        // calculate xMax for the max displayed value
        xMax = (math.ceil(xMax / unitX) * unitX).toLong
    }

    def calculateYMin(data: Iterable[Double]): Double = {
        data.reduceLeft((l,r) => if (l < r) l else r)
    }

    def calculateYMinMulti(data: Iterable[ProcessedData]): ValueType = {
        val mins = data.map((d: ProcessedData) => calculateYMin(d.data.map((x: TaggedDataPoint) => x.value.value)))
        calculateYMin(mins)
    }

    def calculateYMax(data: Iterable[Double]): Double = {
        data.reduceLeft((l,r) => if (l > r) l else r)
    }

    def calculateYMaxMulti(data: Iterable[ProcessedData]): ValueType = {
        val maxs = data.map((d: ProcessedData) => calculateYMax(d.data.map((x: TaggedDataPoint) => x.value.value)))
        calculateYMax(maxs)
    }

    def calculateUnitY(): Double = {
        val maxAmountPoints = heightChart / minDistanceY
        val deltaY = yMax - yMin
        var unit = 1.0

        var result = deltaY / unit

        // reduce amount until number of points is higher than the max allowed number of points
        while(result < maxAmountPoints) {
            unit /= 10
            result = deltaY / unit
        }

        // increase amount until number of points is lower than the max allowed number of points
        while(result > maxAmountPoints) {
            unit *= 10
            result = deltaY / unit
        }

        // the unit leads now to the number of points that is as close as possible
        // but smaller than the max allowed number of points
        unit
    }

    def calculateUnitX(): Long = {
        ???
    }

    def calculateViewBoxString(): String = {
        val x = - borderLeft
        val y = - borderTop - yMax
        val w = width + x
        val h = height + y

        "" + x + " " + y + " " + w + " " + h
    }

    def apply(config: Config): LineChartConfig.LineChart = {
        preProcessing(config)
        LineChartConfig.LineChart(this, config.getDatasets(ids))
    }

}
