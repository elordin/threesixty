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

/**
  * The config class for a [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]].
  *
  * @author Thomas Engel
  */
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
            "borderRight", "distanceTitle", "angleStart", "angleEnd", "radius", "innerRadiusPercent", "showValues",
            "fontSizeTitle", "fontSize", "widthLegendSymbol", "distanceLegend")
        jsonString.parseJson.convertTo[PieChartConfig]
    }

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = None, //TODO Aggregation
            requiredGoal = None //TODO NoGoal
        )))


    /**
      * This class creates the svg element for a pie chart.
      *
      * @param config the pie chart config
      * @param data the data
      *
      * @author Thomas Engel
     */
    case class PieChart(config: PieChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {

        def toSVG: Elem = ???
        /*
        def getConfig: PieChartConfig = config

        /**
          * @param index the index of the legend entry
          * @return the path (<path d=.. />) for the legend symbol
         */
        private def calculateLegendRectangle(index: Int): String = {
            val wLegendSym = config.widthLegendSymbol
            val xLeft = config.rightLimit
            val yTop = config.upperLimit + config.distanceLegend + index * 2 * wLegendSym

            "M " + xLeft + " " + yTop +
            " L " + (xLeft + wLegendSym) + " " + yTop +
            " L " + (xLeft + wLegendSym) + " " + (yTop + wLegendSym) +
            " L " + xLeft + " " + (yTop + wLegendSym)
        }

        /**
          * @return a list of svg elements that should be included into the chart
         */
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


/**
  * The config to create a [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]].
  * The start and end angle have to be between -360° and 360°.
  *
  * @param ids set of ids which are to be displayed in the visualization
  * @param height the height
  * @param width the width
  * @param title the title
  * @param borderTop the border to the top
  * @param borderBottom the border to the bottom
  * @param borderLeft the border to the left
  * @param borderRight the border to the right
  * @param distanceTitle the distance between the title and the top of the chart
  * @param angleStart the start angle
  * @param angleEnd the end angle
  * @param radius the radius
  * @param innerRadiusPercent the percent of the inner radius wihich is cutted out
  * @param showValues iff the values should be shown. Otherwise the percent values are shown
  * @param fontSizeTitle the font size of the title
  * @param fontSize the font size of labels
  * @param widthLegendSymbol the width and height of the legend symbol
  * @param distanceLegend the distance between the top of the legend and the top of the chart within the border
  *
  * @author Thomas Engel
  */
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

    /**
     * @return a default value for borderTop
     */
    override def borderTopDefault: Int = 100

    /**
     * @return a default value for borderRight
     */
    override def borderRightDefault: Int = 150

    /**
     * @return the start angle or a default value
     */
    def angleStart: Int = _angleStart.getOrElse(90) % 360
    /**
     * @return the end angle or a default value
     */
    def angleEnd: Int = _angleEnd.getOrElse(-270) % 360

    /**
     * @return the radius or a calculated default value
     */
    def radius: Double = _radius.getOrElse(calculateRadius)
    /**
     * @return the innerRadiusPercent or the default value 0
     */
    def innerRadiusPercent: Double = _innerRadiusPercent.getOrElse(0)
    /**
     * @return the calculated inner radius
     */
    def innerRadius: Double = radius * innerRadiusPercent

    require(radius > 0, "Value for radius must be greater than 0.")
    require(innerRadius >= 0, "Negative value for innerRadius is not allowed.")

    /**
     * @return showValues or the default value false
     */
    def showValues: Boolean = _showValues.getOrElse(false)

    /**
     * @return the distanceLegend or a default value
     */
    def distanceLegend: Int = _distanceLegend.getOrElse(20)
    /**
     * @return the widthLegendSymbol or a default value
     */
    def widthLegendSymbol: Int = _widthLegendSymbol.getOrElse(10)

    /*
    var segments = calculateSegments
    */


    // var segments: List[Segment] = List.empty


    /**
      * @return a list of important angles needed to calculate the radius
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

    /**
      * @return a tuple containing the minimum and maximum value on the x-axis
     */
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

    /**
      * @return a tuple containing the minimum and maximum value on the y-axis
     */
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

    /**
      * @return the maximal possible radius considering the x-dimension
     */
    private def getMaxRadiusX: Double = {
        val (xleft, xright) = calculateXRange
        val maxXRange = math.max(math.abs(xleft), math.abs(xright))

        chartWidth * (maxXRange / (xright - xleft))
    }

    /**
      * @return the maximal possible radius considering the y-dimension
     */
    private def getMaxRadiusY: Double = {
        val (ytop, ybottom) = calculateYRange
        val maxYRange = math.max(math.abs(ytop), math.abs(ybottom))

        chartHeight * (maxYRange / (ybottom - ytop))
    }

    /**
      * @return the maximal possible radius considering both dimensions
     */
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

    /**
      * @return the difference between the start and end angle
     */
    private def getDeltaAngles: Double = {
        angleEnd - angleStart
    }

    /**
      * @param data the data
      * @return the total sum of all the values contained in the data
     */
    private def calculateSumValues(data: ProcessedData): Double = {
        data.dataPoints.map((p: TaggedDataPoint) => p.value.value).sum
    }

    /**
      * @param data the data
      * @return a map containing the value for each datapoint
     */
    private def getRealValues(data: ProcessedData): Map[String, String] = {
        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.toString) (collection.breakOut): Map[String, String]
    }

    /**
      * @param data the data
      * @return a map containing the percentual value for each datapoint
     */
    private def calculatePercentValues(data: ProcessedData): Map[String, Double] = {
         val total = calculateSumValues(data)
        data.dataPoints.map(
            (p: TaggedDataPoint) =>
                p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.value / total) (collection.breakOut): Map[String, Double]
    }

    /**
      * Calculates the list of [[Segment]]s and assigns them to the corresponding variable.
      *
      * @param data the data
     */
    private def calculateSegments(data: ProcessedData): Unit = {
        var result: List[Segment] = List.empty

        var sAngle: Double = angleStart
        val deltaAngle = getDeltaAngles

        val percentMap = calculatePercentValues(data)
        val valueMap = getRealValues(data)

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

        segments = result.reverse
    }

    /**
      * Please note: The method [[calculateSegments()]] has to be invoked previously to get
      * the proper list of [[Segment]]s. Otherwise an empty list will be returned.
      *
      * @return the list of [[Segment]]s
     */
    def getSegments: List[Segment] = {
        segments
    }

    */


    /**
      * Calculates the list of [[Segment]]s and returns the
      * [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]]
      * for this configuration.
      *
      * @param pool the pool containing the data
      * @return the [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]] for this configuration
     */
    def apply(pool: DataPool): PieChartConfig.PieChart = {
        //val data = pool.getDatasets(ids).head
        val data = dataTest

        // calculateSegments(data)

        PieChartConfig.PieChart(this, pool.getDatasets(ids))
    }

}
