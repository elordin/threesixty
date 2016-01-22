package threesixty.visualizer.visualizations.PieChart

import threesixty.data.TimestampJsonProtocol._
import threesixty.data.tags.{Tag, AggregationTag}
import threesixty.data.{TaggedDataPoint, ProcessedData}
import threesixty.data.Data.{DoubleValue, Timestamp, Identifier}
import threesixty.visualizer._
import threesixty.config.Config
import spray.json._


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationInfo] =
        super.visualizationInfos + ("piechart" ->
            VisualizationInfo(
                "PieChart",
                { json:String => PieChartConfig.apply(json) },
                "Parameters: \n" +
                "    height:        Int                  - Height of the diagram in px\n" +
                "    width:         Int                  - Width of the diagram in px\n" +
                "    title:         String    (optional) - Diagram title\n" +
                "    borderTop:     Int       (optional) - Border to the top in px\n" +
                "    borderBottom:  Int       (optional) - Border to the bottom in px\n" +
                "    borderLeft:    Int       (optional) - Border to the left in px\n" +
                "    borderRight:   Int       (optional) - Border to the right in px\n" +
                "    angleStart     Int       (optional) - The start angle\n" +
                "    angleEnd       Int       (optional) - The end angle\n" +
                "    radius         Double    (optional) - The radius\n" +
                "    innerRadius    Double    (optional) - Radius for cutting out a circle\n" +
                "    showValues     Boolean   (optional) - If values should be shown"
            )
        )
}


object PieChartConfig {

    /**
      *  Public constructor that parses JSON into a PieChartConfig
      *  @param jsonString representation of the config
      *  @return PieChartConfig with all arguments from the JSON set
      */
    def apply(jsonString: String): PieChartConfig = {
        implicit val pieChartConfigFormat = jsonFormat(PieChartConfig.apply,
            "ids", "height", "width", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "angleStart", "angleEnd", "radius", "innerRadius", "showValues")
        jsonString.parseJson.convertTo[PieChartConfig]
    }


    case class PieChart(config: PieChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        var strokeIndex = -1
        val strokes = List("#222222","#444444", "#666666", "#888888", "#AAAAAA", "#CCCCCC", "#EEEEEE")
        var strokeMap: Map[String, Int] = Map.empty

        def radiusForLabel: Double = config._radius + 20

        def calculatePath(data: (String, Double, Double, Int)): String = {
            val (name, sAngle, eAngle, largeArcFlag) = data
            val sweepFlag = config.sweepFlag
            val innerSweepFlag = if(sweepFlag == 0) 1 else 0

            val p1 = config.calculatePoint(sAngle, config._innerRadius)
            val p2 = config.calculatePoint(sAngle, config._radius)
            val p3 = config.calculatePoint(eAngle, config._radius)
            val p4 = config.calculatePoint(eAngle, config._innerRadius)

            "M " + p1._1 + " " + p1._2 +
            " L " + p2._1 + " " + p2._2 + " " +
            " A " + config._radius + " " + config._radius + " 0 " + largeArcFlag + " " + sweepFlag + " " + p3._1 + " " + p3._2 +
            " L " + p4._1 + " " + p4._2 +
            " A " + config._innerRadius + " " + config._innerRadius + " 0 " + largeArcFlag + " " + innerSweepFlag + " " + p1._1 + " " + p1._2
        }

        def calculateLabelPath(data: (String, Double, Double, Int)): String = {
            val (_, sAngle, eAngle, _) = data
            val angle = sAngle + (eAngle - sAngle) / 2.0

            val (px, py) = config.calculatePoint(angle, config._radius)
            val (zx, zy) = config.calculatePoint(angle, radiusForLabel - 3)

            "M " + px + " " + py + " L " + zx + " " + zy
        }

        def calculateLabelTextPointAndTextAnchor(data: (String, Double, Double, Int)): (Double, Double, String) = {
            val (_, sAngle, eAngle, _) = data
            val angle = sAngle + (eAngle - sAngle) / 2.0

            val (zx, zy) = config.calculatePoint(angle, radiusForLabel)

            val direction = math.signum(config.calculateXCoordinate(angle, 1))
            val anchor = if(direction < 0) "end" else "start"

            (zx - direction * 75 - (if(direction < 0) 12 else 0), zy, anchor)
        }

        def calculateLegendRectangle(index: Int, data: (String, Double, Double, Int)): String = {
            val xLeft = config.rightLimit + 40
            val yTop = config.upperLimit + 2 + index*20

            "M " + xLeft + " " + yTop +
            " L " + (xLeft + 10) + " " + yTop +
            " L " + (xLeft + 10) + " " + (yTop + 10) +
            " L " + xLeft + " " + (yTop + 10)
        }

        def getStroke(name: String): String = {
            var index = 0
            if(strokeMap.contains(name)) {
                index = strokeMap.get(name).get
            } else {
                strokeIndex = (strokeIndex+1) % strokes.size
                strokeMap = strokeMap + (name -> strokeIndex)
                index = strokeIndex
            }
            strokes(index)
        }

        def toSVG: xml.Elem = {
            val (vbX, vbY, width, height) = config.calculateViewBox()

            // TODO: for testing only!!!
            val data1 = Set(
                new ProcessedData("aggregatedData", List(
                    new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
                    new TaggedDataPoint(new Timestamp(0), new DoubleValue(10), Set(new AggregationTag("Wert 2"))),
                    new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
                    new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))
            )

            val percentMap = config.calculatePercentValues(data1.head)
            val preparedData = config.calculateAngles(percentMap)

            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox={vbX + " " + vbY + " " + width + " " + height} xml:space="preserve">
                <g id="datapoints">
                    {for (datapoint <- preparedData) yield
                        <path id={datapoint._1}
                              fill={getStroke(datapoint._1)}
                              d={calculatePath(datapoint)} />
                        <path stroke="#000000"
                              d={calculateLabelPath(datapoint)}/>
                        <text x={calculateLabelTextPointAndTextAnchor(datapoint)._1.toString}
                              y={calculateLabelTextPointAndTextAnchor(datapoint)._2.toString}
                              font-family="Roboto, Segoe UI"
                              font-weight="100"
                              font-size="10"
                              text-anchor={calculateLabelTextPointAndTextAnchor(datapoint)._3}>
                            {if(config._showValues) config.getRealValues(data1.head)(datapoint._1) else (math.rint(1000*percentMap.get(datapoint._1).get)/10.0 + " %")}
                        </text>
                    }
                </g>
                <g id="legend">
                    {for (i <- 0 until preparedData.size) yield
                        <path id={preparedData(i)._1 + "Legend"}
                              d={calculateLegendRectangle(i, preparedData(i))}
                              stroke={getStroke(preparedData(i)._1)}
                              stroke-width="0"
                              fill={getStroke(preparedData(i)._1)}/>
                        <text x={(config.rightLimit + 30 - 50).toString}
                              y={(config.upperLimit + 10 + i*20).toString}
                              font-family="Roboto, Segoe UI"
                              font-weight="100"
                              font-size="10"
                              text-anchor="left">
                            {preparedData(i)._1}
                        </text>
                    }
                </g>
                <text x={(vbX + config.width / 2.0).toString}
                      y={(config.upperLimit - 48).toString}
                      font-family="Roboto, Segoe UI"
                      font-weight="100"
                      font-size="20"
                      text-anchor="middle">
                    {config._title}
                </text>
            </svg>
        }
    }
}


case class PieChartConfig private (
    val ids:            Set[Identifier],
    val height:         Int,
    val width:          Int,
    val title:          Option[String] = None,
    val borderTop:      Option[Int]    = None,
    val borderBottom:   Option[Int]    = None,
    val borderLeft:     Option[Int]    = None,
    val borderRight:    Option[Int]    = None,
    val angleStart:     Option[Int]    = None,
    val angleEnd:       Option[Int]    = None,
    val radius:         Option[Double] = None,
    val innerRadius:    Option[Double] = None,
    val showValues:     Option[Boolean]= None
) extends VisualizationConfig(
    ids: Set[Identifier],
    height,
    width,
    title,
    borderTop,
    borderBottom,
    borderLeft,
    borderRight) {

    override def borderTopDefault: Int = 100
    override def borderRightDefault: Int = 150

    override def calculateOrigin: (Double, Double) = {
        (_borderLeft + widthChart / 2.0, _borderTop + heightChart / 2.0)
    }

    def _angleStart: Int = angleStart.getOrElse(90)
    def _angleEnd: Int = angleEnd.getOrElse(-270)

    def _radius: Double = radius.getOrElse(calculateRadius)
    def _innerRadius: Double = innerRadius.getOrElse(0)

    require(_radius > 0, "Value for radius must be greater than 0.")
    require(_innerRadius >= 0, "Negative value for innerRadius is not allowed.")

    def _showValues: Boolean = showValues.getOrElse(false)

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        )))

    def calculateRadius: Double = {
        math.min(heightChart / 2.0, widthChart / 2.0)
    }

    def getDeltaAngles: Double = {
        _angleEnd - _angleStart
    }

    def calculateSumValues(data: ProcessedData): Double = {
        data.dataPoints.map((p: TaggedDataPoint) => p.value.value).sum
    }

    def getRealValues(data: ProcessedData): Map[String, String] = {
        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.toString) (collection.breakOut): Map[String, String]
    }

    def calculatePercentValues(data: ProcessedData): Map[String, Double] = {
        val total = calculateSumValues(data)
        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.value / total) (collection.breakOut): Map[String, Double]
    }

    def calculateAngles(data: Map[String, Double]): List[(String, Double, Double, Int)] = {
        var result: List[(String, Double, Double, Int)] = List()

        var sAngle: Double = _angleStart
        val deltaAngle = getDeltaAngles

        for(entry <- data) {
            val dAngle = entry._2 * deltaAngle
            val start = sAngle
            sAngle += dAngle
            val end = sAngle

            val largeArcFlag = if(math.abs(dAngle) > 180) 1 else 0

            result = (entry._1, start, end, largeArcFlag) :: result
        }

        result.reverse
    }

    def sweepFlag: Int = {
        if(getDeltaAngles < 0) {
            1
        } else {
            0
        }
    }

    def calculateXCoordinate(angle: Double, radius: Double): Double = {
        radius * math.cos(math.toRadians(angle))
    }

    def calculateYCoordinate(angle: Double, radius: Double): Double = {
        - radius * math.sin(math.toRadians(angle))
    }

    def calculatePoint(angle: Double, radius: Double): (Double, Double) = {
        (calculateXCoordinate(angle, radius), calculateYCoordinate(angle, radius))
    }

    def apply(config: Config): PieChartConfig.PieChart = {
        PieChartConfig.PieChart(this, config.getDatasets(ids))
    }

}
