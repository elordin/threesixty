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
                    "    height:        Int                 - Height of the diagram in px\n" +
                    "    width:         Int                 - Width of the diagram in px\n" +
                    "    optXMin:       Option[Timestamp]   - Minimum value of the x-axis\n" +
                    "    optXMax:       Option[Timestamp]   - Maximum value of the x-axis\n" +
                    "    optYMin:       Option[ValueType]   - Minimum value of the y-axis\n" +
                    "    optYMax:       Option[ValueType]   - Maximum value of the y-axis\n" +
                    "    xLabel:        String              - Label for the x-axis\n" +
                    "    yLabel:        String              - Label for the y-axis\n" +
                    "    title:         String              - Diagram title\n" +
                    "    borderTop:     Int                 - Border to the top in px\n" +
                    "    borderBottom:  Int                 - Border to the bottom in px\n" +
                    "    borderLeft:    Int                 - Border to the left in px\n" +
                    "    borderRight:   Int                 - Border to the right in px\n" +
                    "    minDistanceX   Int                 - Minimum number of px between two control points on the x-axis\n" +
                    "    minDistanceY   Int                 - Minimum number of px between two control points on the y-axis\n" +
                    "    optUnitX       Option[String]      - Name of the desired unit on the x-axis\n" +
                    "    optUnitY       Option[Double]      - Value of the desired unit on the y-axis\n"
                )
            )
    }


    /**
     *  Public constructor that parses JSON into a configuration
     *  @param json representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(json: String): LineChartConfig = new LineChartConfig(
        Set("lineTest", "data1", "data2"),
        950, 1200, borderRight = 150,
        title="Test Chart mit etwas mehr Text", yLabel = "Werte", xLabel = "Zeit") // TODO actually read JSON


    case class LineChart(config: LineChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        private def calculateTextYOffset(value: String): Int = {
            - value.size * 8
        }

        private def yCoordTextToString(value: Double): String = {
            if(value == 0) {
                value.toInt.toString
            } else if (math.abs(value) < 1) {
                value.toString
            } else {
                value.toInt.toString
            }
        }

        private def calculatePath(config: LineChartConfig, data: ProcessedData): String = {
            var path = ""

            for(d <- data.dataPoints) {
                if(path.isEmpty) {
                    path += "M " + config.convertXPoint(d.timestamp.getTime) + " " + config.convertYPoint(d.value.value)
                } else {
                    path += " L " + config.convertXPoint(d.timestamp.getTime) + " " + config.convertYPoint(d.value.value)
                }
            }

            path
        }

        def toSVG: Elem = {
            val (vbX, vbY, vbWidth, vbHeight) = config.calculateViewBox()

            val lowerLimit = vbHeight - config.borderBottom + vbY
            val upperLimit = vbY + config.borderTop

            val leftLimit = 0
            val rightLimit = vbWidth - config.borderLeft - config.borderRight

            val textVerticalOffsetY = 5
            val textHorizontalOffsetY = -100

            val textVerticalOffsetX = 20
            val textHorizontalOffsetX = -140

            val unitXAxis = config.unitX.getUnit

            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox={vbX + " " + vbY + " " + vbWidth + " " + vbHeight} xml:space="preserve">
                <g id="grid">
                    // background-grid y-axis
                    {for (i <- config.yMin to config.yMax by config.unitY) yield
                        <line fill="none" stroke={if(i==0) "#000000" else "#AAAAAA"} stroke-dasharray={if (i==0) "0,0" else "5,5"} x1={leftLimit.toString} y1={config.convertYPoint(i).toString} x2={rightLimit.toString} y2={config.convertYPoint(i).toString} />
                        <text x={(vbX + textHorizontalOffsetY + calculateTextYOffset(yCoordTextToString(i))).toString} y={(config.convertYPoint(i) + textVerticalOffsetY).toString} font-family="Roboto, Segoe UI" font-weight="100" font-size="16">
                            {yCoordTextToString(i)}
                        </text>
                    }
                    // background-grid x-axis
                    {for (i <- 0 to config.amountXPoints) yield
                        <line fill="none" stroke={if(i==0) "#000000" else "#AAAAAA"} stroke-dasharray={if (i==0) "0,0" else "5,5"} x1={(i*config.stepX).toString} y1={lowerLimit.toString} x2={(i*config.stepX).toString} y2={upperLimit.toString} />
                        <text x={(i*config.stepX + textHorizontalOffsetX).toString} y={(lowerLimit + textVerticalOffsetX).toString} font-family="Roboto, Segoe UI" font-weight="100" font-size="16">
                            {config.unitX.getLabel(i, config.xMin + i*config.unitX.getTotalMillis)}
                        </text>
                    }
                </g>
                // data
                {for (dataset <- data) yield
                <g id={dataset.id}>
                    <g id="datapoints">
                        {for (datapoint <- dataset.dataPoints) yield
                            <circle fill="#00008B" stroke="#00008B" cx={config.convertXPoint(datapoint.timestamp.getTime).toString} cy={config.convertYPoint(datapoint.value.value).toString} r="4"/>}
                    </g>
                    <path stroke="#6495ED" fill="none" stroke-width="2" d={calculatePath(config, dataset)}/>
                </g>}
                // chart title
                <text x={(vbX - 320 + (config.width - config.title.size * 20) / 2).toString} y={(vbY + 60).toString} font-family="Roboto, Segoe UI" font-weight="100" font-size="48">
                    {config.title}
                </text>
                // y-label
                <text x={(vbX + textHorizontalOffsetY + 20 + calculateTextYOffset(config.yLabel) / 2).toString} y={(vbY + 75).toString} font-family="Roboto, Segoe UI" font-size="20">
                    {config.yLabel}
                </text>
                // x-label
                <text x={(rightLimit - 85).toString} y={(lowerLimit + textVerticalOffsetX - 15).toString} font-family="Roboto, Segoe UI" font-size="20">
                    {config.xLabel}
                    {if (!unitXAxis.isEmpty)
                    {
                        <tspan x={(rightLimit - 140).toString} y={(lowerLimit + textVerticalOffsetX + 5).toString}>
                            {"(in " + unitXAxis + ")"}
                        </tspan>
                    }}
                </text>
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
    val borderTop: Int = 100,
    val borderBottom: Int = 50,
    val borderLeft: Int = 50,
    val borderRight: Int = 50,
    val minDistanceX: Int = 50,
    val minDistanceY: Int = 50,
    val optUnitX: Option[String] = None,
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
        yMin = math.floor(yMin / unitY) * unitY
        yMax = math.ceil(yMax / unitY) * unitY

        // calculate the distance between two control points on the x-axis
        unitX = optUnitX match {
            case None => calculateUnitX()
            case Some(name) => getUnitX(name)
        }
        amountXPoints = math.ceil((1.0*(xMax - xMin)) / unitX.getTotalMillis).toInt
        stepX = (1.0*widthChart) / amountXPoints

        // calculate xMax for the max displayed value
        xMax = (math.ceil((1.0*xMax) / unitX.getTotalMillis) * unitX.getTotalMillis).toLong
    }

    def calculateYMin(data: Iterable[Double]): Double = {
        data.reduceLeft((l,r) => if (l < r) l else r)
    }

    def calculateYMinMulti(data: Iterable[ProcessedData]): ValueType = {
        val mins = data.map((d: ProcessedData) => calculateYMin(d.dataPoints.map((x: TaggedDataPoint) => x.value.value)))
        calculateYMin(mins)
    }

    def calculateYMax(data: Iterable[Double]): Double = {
        data.reduceLeft((l,r) => if (l > r) l else r)
    }

    def calculateYMaxMulti(data: Iterable[ProcessedData]): ValueType = {
        val maxs = data.map((d: ProcessedData) => calculateYMax(d.dataPoints.map((x: TaggedDataPoint) => x.value.value)))
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

    def getPossibleXScaling: List[XScaling] = List(
        new XScalingMillis1, new XScalingMillis10, new XScalingMillis100,
        new XScalingSeconds1, new XScalingSeconds10, new XScalingSeconds30,
        new XScalingMinutes1, new XScalingMinutes10, new XScalingMinutes30,
        new XScalingHours1, new XScalingHours3, new XScalingHours6, new XScalingHours12,
        new XScalingDays1, new XScalingDays7,
        new XScalingMonths1, new XScalingMonths3, new XScalingMonths6,
        new XScalingYears1, new XScalingYears5, new XScalingYears10
    )

    def calculateUnitX(): XScaling = {
        val maxAmountPoints = widthChart / minDistanceX
        val deltaX = xMax - xMin

        for (unit <- getPossibleXScaling) {
            val result = (1.0*deltaX) / unit.getTotalMillis
            if(result <= maxAmountPoints) {
                return unit
            }
        }

        throw new UnsupportedOperationException("No scaling for the x-axis could be found.")
    }

    def getUnitX(name: String): XScaling = {
        val possible = getPossibleXScaling.filter((x: XScaling) => x.name.equals(name))
        if(possible.size > 0) {
            possible.head
        } else {
            throw new NoSuchElementException("No unit with the name '" + name + "' could be found.")
        }
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
