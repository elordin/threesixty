package threesixty.visualizer.visualizations.LineChart

import threesixty.config.Config
import threesixty.data.Data.{Identifier, Timestamp, ValueType}
import threesixty.data.metadata.{Scaling, Timeframe}
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.visualizer.{DataRequirement, Visualization, VisualizationConfig, VisualizationInfo, VisualizationMetadata, VisualizationMixins}

import spray.json._
import threesixty.data.TimestampJsonProtocol._

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationInfo] =
        super.visualizationInfos + ("linechart" ->
            VisualizationInfo(
                "LineChart",
                { json:String => LineChartConfig.apply(json) },
                "LineChart\n" +
                "  Parameters: \n" +
                "    height:        Int                  - Height of the diagram in px\n" +
                "    width:         Int                  - Width of the diagram in px\n" +
                "    optXMin:       Timestamp (optional) - Minimum value of the x-axis\n" +
                "    optXMax:       Timestamp (optinmal) - Maximum value of the x-axis\n" +
                "    optYMin:       Double    (optional) - Minimum value of the y-axis\n" +
                "    optYMax:       Double    (optional) - Maximum value of the y-axis\n" +
                "    xLabel:        String    (optional) - Label for the x-axis\n" +
                "    yLabel:        String    (optional) - Label for the y-axis\n" +
                "    title:         String    (optional) - Diagram title\n" +
                "    borderTop:     Int       (optional) - Border to the top in px\n" +
                "    borderBottom:  Int       (optional) - Border to the bottom in px\n" +
                "    borderLeft:    Int       (optional) - Border to the left in px\n" +
                "    borderRight:   Int       (optional) - Border to the right in px\n" +
                "    minDistanceX   Int       (optional) - Minimum number of px between two control points on the x-axis\n" +
                "    minDistanceY   Int       (optional) - Minimum number of px between two control points on the y-axis\n" +
                "    optUnitX       String    (optional) - Name of the desired unit on the x-axis\n" +
                "    optUnitY       Double    (optional) - Value of the desired unit on the y-axis\n"
            )
        )
}


object LineChartConfig {


    /**
     *  Public constructor that parses JSON into a LineChartConfig
     *  @param json representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): LineChartConfig = {
        implicit val lineChartConfigFormat = jsonFormat(LineChartConfig.apply,
            "ids", "height", "width", "optXMin", "optXMax", "optYMin", "optYMax",
            "xLabel", "yLabel", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "minDistanceX", "minDistanceY", "optUnitX", "optUnitY")
        jsonString.parseJson.convertTo[LineChartConfig]
    }

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

            val lowerLimit = vbHeight - config._borderBottom + vbY
            val upperLimit = vbY + config._borderTop

            val leftLimit = 0
            val rightLimit = vbWidth - config._borderLeft - config._borderRight

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
                <text x={(vbX - 320 + (config.width - config._title.size * 20) / 2).toString} y={(vbY + 60).toString} font-family="Roboto, Segoe UI" font-weight="100" font-size="48">
                    {config._title}
                </text>
                // y-label
                <text x={(vbX + textHorizontalOffsetY + 20 + calculateTextYOffset(config._yLabel) / 2).toString} y={(vbY + 75).toString} font-family="Roboto, Segoe UI" font-size="20">
                    {config._yLabel}
                </text>
                // x-label
                <text x={(rightLimit - 85).toString} y={(lowerLimit + textVerticalOffsetX - 15).toString} font-family="Roboto, Segoe UI" font-size="20">
                    {config._xLabel}
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
    val minDistanceX: Option[Int]       = None,
    val minDistanceY: Option[Int]       = None,
    val optUnitX:     Option[String]    = None,
    val optUnitY:     Option[Double]    = None
) extends VisualizationConfig(
    ids: Set[Identifier],
    height,
    width,
    title,
    borderTop,
    borderBottom,
    borderLeft,
    borderRight) {

    def _xLabel: String = xLabel.getOrElse("")
    def _yLabel: String = yLabel.getOrElse("")

    def _minDistanceX: Int = minDistanceX.getOrElse(50)
    def _minDistanceY: Int = minDistanceY.getOrElse(50)

    require(_minDistanceX > 0, "Value for minDistanceX must be positive.")
    require(_minDistanceY > 0, "Value for minDistanceY must be positive.")

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

        yMin = optYMin.getOrElse(calculateYMinMulti(config.getDatasets(ids)).value)
        yMax = optYMax.getOrElse(calculateYMaxMulti(config.getDatasets(ids)).value)

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

        // calculate xMin and xMax for the min/max displayed value
        xMax = (math.ceil((1.0*xMax) / unitX.getTotalMillis) * unitX.getTotalMillis).toLong
        xMin = unitX.getXMin(xMin)
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
        val maxAmountPoints = heightChart / _minDistanceY
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
        val maxAmountPoints = widthChart / _minDistanceX
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

    override def calculateOrigin: (Int, Int) = {
        (_borderLeft, _borderTop - math.ceil(convertYPoint(yMax)).toInt)
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
