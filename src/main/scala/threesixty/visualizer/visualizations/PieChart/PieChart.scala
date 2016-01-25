package threesixty.visualizer.visualizations.PieChart

import spray.json._
import threesixty.config.Config
import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{AggregationTag, Tag}
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.visualizer._

import scala.xml.Elem


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("piechart" -> PieChartConfig)
}


object PieChartConfig extends VisualizationCompanion {

    def name = "PieChart"

    def usage = "Parameters: \n" +
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

    def fromString: (String) => VisualizationConfig = { s => apply(s) }

    /**
      *  Public constructor that parses JSON into a PieChartConfig
      *  @param jsonString representation of the config
      *  @return PieChartConfig with all arguments from the JSON set
      */
    def apply(jsonString: String): PieChartConfig = {
        implicit val pieChartConfigFormat = jsonFormat(PieChartConfig.apply,
            "ids", "height", "width", "title", "borderTop", "borderBottom", "borderLeft",
            "borderRight", "distanceTitle", "angleStart", "angleEnd", "radius", "innerRadius", "showValues",
            "fontSizeTitle", "fontSize", "wLegendSymbol", "distanceLegend")
        jsonString.parseJson.convertTo[PieChartConfig]
    }


    case class PieChart(config: PieChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def getConfig: PieChartConfig = config

        def calculateLegendRectangle(index: Int): String = {
            val wLegendSym = config._wLegendSymbol
            val xLeft = config.rightLimit
            val yTop = config.upperLimit + config._distanceLegend + index * 2 * wLegendSym

            "M " + xLeft + " " + yTop +
            " L " + (xLeft + wLegendSym) + " " + yTop +
            " L " + (xLeft + wLegendSym) + " " + (yTop + wLegendSym) +
            " L " + xLeft + " " + (yTop + wLegendSym)
        }

        def getSVGElements: List[Elem] = {
            List(
                <g id="segments">
                    {for (seg <- config.getSegments) yield
                        seg.getSVGElement
                    }
                </g>,
                <g id="legend">
                    {for (i <- 0 until config.getSegments.size) yield
                        <path id={"Legend" + config.getSegments(i).id}
                              d={calculateLegendRectangle(i)}
                              stroke={config.getSegments(i).getColor}
                              stroke-width="0"
                              fill={config.getSegments(i).getColor}/>
                        <text x={(config.rightLimit + 2*config._wLegendSymbol).toString}
                              y={(config.upperLimit + config._distanceLegend + (2 * i + 1) * config._wLegendSymbol).toString}
                              font-family="Roboto, Segoe UI"
                              font-weight="100"
                              font-size={config.getSegments(i).getFontSize}
                              text-anchor="left">{config.getSegments(i).description}</text>
                    }
                </g>
            )
        }
    }
}


case class PieChartConfig(
    val ids:                Set[Identifier],
    val height:             Int,
    val width:              Int,
    val title:              Option[String] = None,
    val borderTop:          Option[Int]    = None,
    val borderBottom:       Option[Int]    = None,
    val borderLeft:         Option[Int]    = None,
    val borderRight:        Option[Int]    = None,
    val distanceTitle:      Option[Int]    = None,
    val angleStart:         Option[Int]    = None,
    val angleEnd:           Option[Int]    = None,
    val radius:             Option[Double] = None,
    val innerRadiusPercent: Option[Double] = None,
    val showValues:         Option[Boolean]= None,
    val fontSizeTitle:      Option[Int]    = None,
    val fontSize:           Option[Int]    = None,
    val wLegendSymbol:      Option[Int]    = None,
    val distanceLegend:     Option[Int]    = None
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

    // TODO: for testing only!!!
    val dataTest = new ProcessedData("aggregatedData", List(
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(10), Set(new AggregationTag("Wert 2"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))

    override def borderTopDefault: Int = 100
    override def borderRightDefault: Int = 150

    def _angleStart: Int = angleStart.getOrElse(90)
    def _angleEnd: Int = angleEnd.getOrElse(-270)

    def _radius: Double = radius.getOrElse(calculateRadius)
    def _innerRadiusPercent: Double = innerRadiusPercent.getOrElse(0)
    def _innerRadius: Double = _radius * _innerRadiusPercent

    require(_radius > 0, "Value for radius must be greater than 0.")
    require(_innerRadius >= 0, "Negative value for innerRadius is not allowed.")

    def _showValues: Boolean = showValues.getOrElse(false)

    def _distanceLegend: Int = distanceLegend.getOrElse(20)
    def _wLegendSymbol: Int = wLegendSymbol.getOrElse(10)

    val strokes = List("#222222", "#444444", "#666666", "#888888", "#AAAAAA", "#CCCCCC")
    var strokeIndex = -1
    var strokeMap: Map[String, String] = Map.empty

    var segments = calculateSegments

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        )))

    private def getAllAngleCandidates: List[Int] = {
        var angles = List(_angleStart, _angleEnd)
        if(Segment.isAngleContained(-270, _angleStart, _angleEnd)) angles = -270 :: angles
        if(Segment.isAngleContained(-180, _angleStart, _angleEnd)) angles = -180 :: angles
        if(Segment.isAngleContained(-90, _angleStart, _angleEnd)) angles = -90 :: angles
        if(Segment.isAngleContained(0, _angleStart, _angleEnd)) angles = 0 :: angles
        if(Segment.isAngleContained(90, _angleStart, _angleEnd)) angles = 90 :: angles
        if(Segment.isAngleContained(180, _angleStart, _angleEnd)) angles = 180 :: angles
        if(Segment.isAngleContained(270, _angleStart, _angleEnd)) angles = 270 :: angles

        angles
    }

    def calculateXRange: (Double, Double) = {
        var angles = getAllAngleCandidates

        val outerValues = angles.map((i: Int) => Segment.calculateXCoordinate(i, 1))
        val innerValues = angles.map((i: Int) => Segment.calculateXCoordinate(i, _innerRadiusPercent))

        val outerMin = outerValues.min
        val outerMax = outerValues.max

        val innerMin = innerValues.min
        val innerMax = innerValues.max

        (math.min(outerMin, innerMin), math.max(outerMax, innerMax))
    }

    def calculateYRange: (Double, Double) = {
        var angles = getAllAngleCandidates

        val outerValues = angles.map((i: Int) => Segment.calculateYCoordinate(i, 1))
        val innerValues = angles.map((i: Int) => Segment.calculateYCoordinate(i, _innerRadiusPercent))

        val outerMin = outerValues.min
        val outerMax = outerValues.max

        val innerMin = innerValues.min
        val innerMax = innerValues.max

        (math.min(outerMin, innerMin), math.max(outerMax, innerMax))
    }

    private def getMaxRadiusX: Double = {
        val (xleft, xright) = calculateXRange
        val maxXRange = math.max(math.abs(xleft), math.abs(xright))

        widthChart * (maxXRange / (xright - xleft))
    }

    private def getMaxRadiusY: Double = {
        val (ytop, ybottom) = calculateYRange
        val maxYRange = math.max(math.abs(ytop), math.abs(ybottom))

        heightChart * (maxYRange / (ybottom - ytop))
    }

    def calculateRadius: Double = {
        math.min(getMaxRadiusX, getMaxRadiusY)
    }

    override def calculateOrigin: (Double, Double) = {
        val maxRx = getMaxRadiusX
        val maxRy = getMaxRadiusY

        val (x1, x2) = calculateXRange
        val dx = x2 - x1
        val xradStart = if(math.abs(x2) < math.abs(x1)) -1 else 1

        val (y1, y2) = calculateYRange
        val dy = y2 - y1
        val yradStart = if (math.abs(y2) < math.abs(y1)) -1 else 1

        val ox = (if(xradStart < 0) _radius else - _radius + widthChart) - xradStart * (math.max(0, dx * (getMaxRadiusX - getMaxRadiusY))) / 2.0
        val oy = (if(yradStart < 0) _radius else - _radius + heightChart) - yradStart * (math.max(0, dy * (getMaxRadiusY - getMaxRadiusX))) / 2.0

        (_borderLeft + ox, _borderTop + oy)
    }

    def getDeltaAngles: Double = {
        _angleEnd - _angleStart
    }

    def calculateSumValues: Double = {
        // val data = config.getDatasets(ids).head
        val data = dataTest

        data.dataPoints.map((p: TaggedDataPoint) => p.value.value).sum
    }

    def getRealValues: Map[String, String] = {
        // val data = config.getDatasets(ids).head
        val data = dataTest

        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.toString) (collection.breakOut): Map[String, String]
    }

    def calculatePercentValues: Map[String, Double] = {
        // val data = config.getDatasets(ids).head
        val data = dataTest

        val total = calculateSumValues
        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.value / total) (collection.breakOut): Map[String, Double]
    }

    private def getStroke(name: String): String = {
        if(strokeMap.contains(name)) {
            strokeMap.get(name).get
        } else {
            strokeIndex = (strokeIndex + 1) % strokes.size
            val color = strokes(strokeIndex)
            strokeMap += name -> color
            color
        }
    }

    private def calculateSegments: List[Segment] = {
        var result: List[Segment] = List()

        var sAngle: Double = _angleStart
        val deltaAngle = getDeltaAngles

        val percentMap = calculatePercentValues
        val valueMap = getRealValues

        for(entry <- percentMap) {
            val dAngle = entry._2 * deltaAngle
            val start = sAngle
            sAngle += dAngle
            val end = sAngle

            val value = if(_showValues) valueMap.get(entry._1).get else math.round(1000*entry._2)/10.0 + " %"

            val segment = new Segment(
                entry._1,
                entry._1,
                start,
                end,
                _radius,
                _innerRadius,
                _radius + 20,
                value,
                Some(_fontSize),
                Some(getStroke(entry._1))
                )

            result = segment :: result
        }

        result.reverse
    }

    def getSegments: List[Segment] = {
        segments
    }

    def apply(config: Config): PieChartConfig.PieChart = {
        PieChartConfig.PieChart(this, config.getDatasets(ids))
    }

}
