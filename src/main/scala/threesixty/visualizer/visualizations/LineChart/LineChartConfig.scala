package threesixty.visualizer.visualizations.LineChart

import threesixty.config.Config
import threesixty.data.Data.{Identifier, Timestamp, ValueType}
import threesixty.data.metadata.{Scaling, Timeframe}
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.visualizer.{DataRequirement, Visualization, VisualizationConfig, VisualizationInfo, VisualizationMetadata, withVisualizationInfos}

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
    def apply(json: String): LineChartConfig = new LineChartConfig(Set("lineTest"), 900, 1100, title="Test Chart") // TODO actually read JSON


    case class LineChart(config: LineChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: Elem = {
            val (vbX, vbY, vbWidth, vbHeight) = config.calculateViewBox()
            val viewBoxString = "" + vbX + " " + vbY + " " + vbWidth + " " + vbHeight

            val lowerLimit = vbHeight - config.borderBottom + vbY
            val upperLimit = vbY + config.borderTop

            val leftLimit = 0
            val rightLimit = vbWidth - config.borderLeft - config.borderRight

            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox={viewBoxString} xml:space="preserve">
                <g id="grid">
                    // background-grid y-axis
                    {for (i <- config.yMin to config.yMax by config.unitY) yield
                        <line fill="none" stroke={if(i==0) "#000000" else "#AAAAAA"} stroke-dasharray={if (i==0) "0,0" else "5,5"} x1={leftLimit.toString} y1={config.convertYPoint(i).toString} x2={rightLimit.toString} y2={config.convertYPoint(i).toString} />}
                    // background-grid x-axis
                    {for (i <- 0 to config.amountXPoints) yield
                        <line fill="none" stroke={if(i==0) "#000000" else "#AAAAAA"} stroke-dasharray={if (i==0) "0,0" else "5,5"} x1={(i*config.stepX).toString} y1={lowerLimit.toString} x2={(i*config.stepX).toString} y2={upperLimit.toString} />}
                </g>
            </svg>
        }
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
    var unitX: XScaling = null
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
            xMin = if (optXMin.isDefined) math.min(xframe.start.getTime, math.max(0, optXMin.get.getTime)) else xframe.start.getTime
            xMax = if (optXMax.isDefined) math.max(xframe.end.getTime, optXMax.get.getTime) else xframe.end.getTime
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
        val vzMin = yMin / math.abs(yMin)
        yMin = vzMin * math.ceil(math.abs(yMin) / unitY) * unitY
        val vzMax = yMax / math.abs(yMax)
        yMax = vzMax * math.ceil(math.abs(yMax) / unitY) * unitY

        // calculate the distance between two control points on the x-axis
        unitX = calculateUnitX()
        amountXPoints = math.ceil((1.0*(xMax - xMin)) / unitX.getTotalMillis).toInt
        stepX = (1.0*widthChart) / amountXPoints

        // calculate xMax for the max displayed value
        xMax = (math.ceil(xMax / unitX.getTotalMillis) * unitX.getTotalMillis).toLong
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

    def calculateUnitX(): XScaling = {
        val maxAmountPoints = widthChart / minDistanceX
        val deltaX = xMax - xMin

        val possibleUnits = List(
            new XScalingMillis1, new XScalingMillis10, new XScalingMillis100,
            new XScalingSeconds1, new XScalingSeconds10, new XScalingSeconds30,
            new XScalingMinutes1, new XScalingMinutes10, new XScalingMinutes30,
            new XScalingHours1, new XScalingHours3, new XScalingHours6, new XScalingHours12,
            new XScalingDays1, new XScalingDays7,
            new XScalingMonths1, new XScalingMonths3, new XScalingMonths6,
            new XScalingYears1, new XScalingYears5, new XScalingYears10
        )

        for (unit <- possibleUnits) {
            val result = (1.0*deltaX) / unit.getTotalMillis
            if(result <= maxAmountPoints) {
                return unit
            }
        }

        throw new UnsupportedOperationException("No scaling for the x-axis could be found.")
    }

    def calculateViewBox(): (Int, Int, Int, Int) = {
        val x = - borderLeft
        val y = - borderTop + math.ceil(convertYPoint(yMax)).toInt
        val w = width
        val h = height

        (x,y,w,h)
    }

    def convertYPoint(y: Double): Double = {
        - (y / (yMax - yMin)) * heightChart
    }

    def convertXPoint(x: Double): Double = {
        ((x - xMin) / (xMax - xMin)) * widthChart
    }

    def apply(config: Config): LineChartConfig.LineChart = {
        preProcessing(config)
        LineChartConfig.LineChart(this, config.getDatasets(ids))
    }

}
