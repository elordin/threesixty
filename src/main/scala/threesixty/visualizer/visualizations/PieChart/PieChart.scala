package threesixty.visualizer.visualizations.pieChart

import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{AggregationTag, Tag}
import threesixty.data.{ProcessedData, TaggedDataPoint, DataPool}
import threesixty.visualizer._
import threesixty.visualizer.visualizations.Segment
import threesixty.visualizer.util.ColorScheme

import scala.xml.Elem

import spray.json._


trait Mixin extends VisualizationMixins {
    abstract override def visualizationInfos: Map[String, VisualizationCompanion] =
        super.visualizationInfos + ("piechart" -> PieChartConfig)
}


object PieChartConfig extends VisualizationCompanion {

    def name = "PieChart"

    def usage = "Parameters: \n" +
                "    ids:               Set[String]          - The data identifiers\n" +
                "    height:            Int                  - Height of the diagram in px\n" +
                "    width:             Int                  - Width of the diagram in px\n" +
                "    title:             String    (optional) - Diagram title\n" +
                "    borderTop:         Int       (optional) - Border to the top in px\n" +
                "    borderBottom:      Int       (optional) - Border to the bottom in px\n" +
                "    borderLeft:        Int       (optional) - Border to the left in px\n" +
                "    borderRight:       Int       (optional) - Border to the right in px\n" +
                "    distanceTitle      Int       (optional) - Distance between the title and the chart in px\n" +
                "    angleStart         Int       (optional) - The start angle\n" +
                "    angleEnd           Int       (optional) - The end angle\n" +
                "    radius             Double    (optional) - The radius\n" +
                "    innerRadiusPercent Double    (optional) - Radius for cutting out a circle in percent of the radius\n" +
                "    showValues         Boolean   (optional) - If values should be shown\n" +
                "    fontSizeTitle      Int       (optional) - Font size of the title\n" +
                "    fontSize           Int       (optional) - Font size of labels\n" +
                "    widthLegendSysmbol Int       (optional) - Width and height of the legend symbol in px\n" +
                "    distanceLegend     Int       (optional) - Gab before the legend in px"


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
            "fontSizeTitle", "fontSize", "widthLegendSymbol", "distanceLegend")
        jsonString.parseJson.convertTo[PieChartConfig]
    }


    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        )))


    case class PieChart(config: PieChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {

        def toSVG: Elem = ???
        /*
        def getConfig: PieChartConfig = config

        private def calculateLegendRectangle(index: Int): String = {
            val wLegendSym = config.widthLegendSymbol
            val xLeft = config.rightLimit
            val yTop = config.upperLimit + config.distanceLegend + index * 2 * wLegendSym

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
                        <text x={(config.rightLimit + 2*config.widthLegendSymbol).toString}
                              y={(config.upperLimit + config.distanceLegend + (2 * i + 1) * config.widthLegendSymbol).toString}
                              font-family="Roboto, Segoe UI"
                              font-weight="100"
                              font-size={config.getSegments(i).getFontSize}
                              text-anchor="left">{config.getSegments(i).description}</text>
                    }
                </g>
            )
        }

        */
    }
}


case class PieChartConfig(
    val ids:                    Set[Identifier],
    val height:                 Int,
    val width:                  Int,
    val _title:                  Option[String] = None,
    val _borderTop:              Option[Int]    = None,
    val _borderBottom:           Option[Int]    = None,
    val _borderLeft:             Option[Int]    = None,
    val _borderRight:            Option[Int]    = None,
    val _distanceTitle:          Option[Int]    = None,
    val _angleStart:             Option[Int]    = None,
    val _angleEnd:               Option[Int]    = None,
    val _radius:                 Option[Double] = None,
    val _innerRadiusPercent:     Option[Double] = None,
    val _showValues:             Option[Boolean]= None,
    val _fontSizeTitle:          Option[Int]    = None,
    val _fontSize:               Option[Int]    = None,
    val _widthLegendSymbol:      Option[Int]    = None,
    val _distanceLegend:         Option[Int]    = None
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

    // TODO: for testing only!!!
    val dataTest = new ProcessedData("aggregatedData", List(
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(10), Set(new AggregationTag("Wert 2"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))

    override def borderTopDefault: Int = 100
    override def borderRightDefault: Int = 150

    def angleStart: Int = _angleStart.getOrElse(90)
    def angleEnd: Int = _angleEnd.getOrElse(-270)

    def radius: Double = _radius.getOrElse(calculateRadius)
    def innerRadiusPercent: Double = _innerRadiusPercent.getOrElse(0)
    def innerRadius: Double = radius * innerRadiusPercent

    require(radius > 0, "Value for radius must be greater than 0.")
    require(innerRadius >= 0, "Negative value for innerRadius is not allowed.")

    def showValues: Boolean = _showValues.getOrElse(false)

    def distanceLegend: Int = _distanceLegend.getOrElse(20)
    def widthLegendSymbol: Int = _widthLegendSymbol.getOrElse(10)

    /*
    var segments = calculateSegments
    */



    private def getAllAngleCandidates: List[Int] = {
        var angles = List(angleStart, angleEnd)
        if(Segment.isAngleContained(-270, angleStart, angleEnd)) angles = -270 :: angles
        if(Segment.isAngleContained(-180, angleStart, angleEnd)) angles = -180 :: angles
        if(Segment.isAngleContained(-90, angleStart, angleEnd)) angles = -90 :: angles
        if(Segment.isAngleContained(0, angleStart, angleEnd)) angles = 0 :: angles
        if(Segment.isAngleContained(90, angleStart, angleEnd)) angles = 90 :: angles
        if(Segment.isAngleContained(180, angleStart, angleEnd)) angles = 180 :: angles
        if(Segment.isAngleContained(270, angleStart, angleEnd)) angles = 270 :: angles

        angles
    }

    private def calculateXRange: (Double, Double) = {
        var angles = getAllAngleCandidates

        val outerValues = angles.map((i: Int) => Segment.calculateXCoordinate(i, 1))
        val innerValues = angles.map((i: Int) => Segment.calculateXCoordinate(i, innerRadiusPercent))

        val outerMin = outerValues.min
        val outerMax = outerValues.max

        val innerMin = innerValues.min
        val innerMax = innerValues.max

        (math.min(outerMin, innerMin), math.max(outerMax, innerMax))
    }

    private def calculateYRange: (Double, Double) = {
        var angles = getAllAngleCandidates

        val outerValues = angles.map((i: Int) => Segment.calculateYCoordinate(i, 1))
        val innerValues = angles.map((i: Int) => Segment.calculateYCoordinate(i, innerRadiusPercent))

        val outerMin = outerValues.min
        val outerMax = outerValues.max

        val innerMin = innerValues.min
        val innerMax = innerValues.max

        (math.min(outerMin, innerMin), math.max(outerMax, innerMax))
    }

    private def getMaxRadiusX: Double = {
        val (xleft, xright) = calculateXRange
        val maxXRange = math.max(math.abs(xleft), math.abs(xright))

        chartWidth * (maxXRange / (xright - xleft))
    }

    private def getMaxRadiusY: Double = {
        val (ytop, ybottom) = calculateYRange
        val maxYRange = math.max(math.abs(ytop), math.abs(ybottom))

        chartHeight * (maxYRange / (ybottom - ytop))
    }
    private def calculateRadius: Double = {
        math.min(getMaxRadiusX, getMaxRadiusY)
    }
/*
    override def calculateOrigin: (Double, Double) = {
        val maxRx = getMaxRadiusX
        val maxRy = getMaxRadiusY

        val (x1, x2) = calculateXRange
        val dx = x2 - x1
        val xradStart = if(math.abs(x2) < math.abs(x1)) -1 else 1

        val (y1, y2) = calculateYRange
        val dy = y2 - y1
        val yradStart = if (math.abs(y2) < math.abs(y1)) -1 else 1

        val ox = (if(xradStart < 0) radius else - radius + chartWidth) - xradStart * (math.max(0, dx * (getMaxRadiusX - getMaxRadiusY))) / 2.0
        val oy = (if(yradStart < 0) radius else - radius + chartHeight) - yradStart * (math.max(0, dy * (getMaxRadiusY - getMaxRadiusX))) / 2.0

        (borderLeft + ox, borderTop + oy)
    }

    private def getDeltaAngles: Double = {
        angleEnd - angleStart
    }

    private def calculateSumValues: Double = {
        // val data = config.getDatasets(ids).head
        val data = dataTest

        data.dataPoints.map((p: TaggedDataPoint) => p.value.value).sum
    }

    private def getRealValues: Map[String, String] = {
        // val data = config.getDatasets(ids).head
        val data = dataTest

        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.toString) (collection.breakOut): Map[String, String]
    }

    private def calculatePercentValues: Map[String, Double] = {
        // val data = config.getDatasets(ids).head
        val data = dataTest

        val total = calculateSumValues
        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.value / total) (collection.breakOut): Map[String, Double]
    }

    private def calculateSegments: List[Segment] = {
        var result: List[Segment] = List()

        var sAngle: Double = angleStart
        val deltaAngle = getDeltaAngles

        val percentMap = calculatePercentValues
        val valueMap = getRealValues

        for(entry <- percentMap) {
            val dAngle = entry._2 * deltaAngle
            val start = sAngle
            sAngle += dAngle
            val end = sAngle

            val value = if(showValues) valueMap.get(entry._1).get else math.round(1000*entry._2)/10.0 + " %"

            val segment = new Segment(
                entry._1,
                entry._1,
                start,
                end,
                radius,
                innerRadius,
                radius + 20,
                value,
                Some(fontSize),
                Some(ColorScheme.next))

            result = segment :: result
        }

        result.reverse
    }

    def getSegments: List[Segment] = {
        segments
    }

    */
    def apply(pool: DataPool): PieChartConfig.PieChart = {
        PieChartConfig.PieChart(this, pool.getDatasets(ids))
    }

}
