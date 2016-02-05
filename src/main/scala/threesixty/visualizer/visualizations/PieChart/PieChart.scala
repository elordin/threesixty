package threesixty.visualizer.visualizations.pieChart

import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.data.Data.{DoubleValue, Identifier, Timestamp}
import threesixty.data.DataJsonProtocol._
import threesixty.data.tags.{AggregationTag, Tag}
import threesixty.data.{ProcessedData, TaggedDataPoint, DataPool}
import threesixty.visualizer._
import threesixty.visualizer.util.LegendPositionType.LegendPosition
import threesixty.visualizer.visualizations.Segment
import threesixty.visualizer.util._

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
                "    ids:                       Set[String]            - The data identifiers\n" +
                "    height:                    Int                    - Height of the diagram in px\n" +
                "    width:                     Int                    - Width of the diagram in px\n" +
                "    border:                    Border      (optional) - Border (top, bottom, left, right) in px\n" +
                "    colorScheme:               String      (optional) - The color scheme\n" +
                "    title:                     String      (optional) - Diagram title\n" +
                "    titleVerticalOffset:       Int         (optional) - The vertical offset of the title\n" +
                "    titleFontSize:             Int         (optional) - The font size of the title\n" +
                "    fontSize:                  Int         (optional) - The font size\n" +
                "    fontFamily:                String      (optional) - The font family\n" +
                "    legendPosition:            String      (optional) - The legend position\n" +
                "    legendHorizontalOffset:    Int         (optional) - The horizontal offset of the legend\n" +
                "    legendVerticalOffset:      Int         (optional) - The vertical offset of the legend\n" +
                "    legendSymbolWidth:         Int         (optional) - The width of the legend symbol\n" +
                "    showSegmentLabels:         Boolean     (optional) - If labels for a segment should be shown\n" +
                "    segmentLabelLineColor:     String      (optional) - The color of the line connecting the segment with the label\n" +
                "    showValues:                Boolean     (optional) - If values should be shown\n" +
                "    angleStart:                Int         (optional) - The start angle\n" +
                "    angleEnd:                  Int         (optional) - The end angle\n" +
                "    radius:                    Double      (optional) - The radius\n" +
                "    innerRadiusPercent:        Double      (optional) - Radius for cutting out a circle in percent of the radius\n"

    def fromString: (String) => VisualizationConfig = { s => apply(s) }


    def default(ids: Seq[Identifier], height: Int, width: Int) = PieChartConfig(ids, height, width)

    /**
      *  Public constructor that parses JSON into a PieChartConfig
      *  @param jsonString representation of the config
      *  @return PieChartConfig with all arguments from the JSON set
     */
    def apply(jsonString: String): PieChartConfig = {
        implicit val pieChartConfigFormat = jsonFormat(PieChartConfig.apply,
            "ids",
            "height", "width",
            "border",
            "colorScheme",
            "title", "titleVerticalOffset", "titleFontSize",
            "fontSize", "fontFamily",
            "legendPosition", "legendHorizontalOffset", "legendVerticalOffset", "legendSymbolWidth",
            "showSegmentLabels", "segmentLabelLineColor", "showValues",
            "angleStart", "angleEnd", "radius", "innerRadiusPercent")
        jsonString.parseJson.convertTo[PieChartConfig]
    }

    val metadata = new VisualizationMetadata(
        List(DataRequirement(
            requiredProcessingMethods = Some(List(Aggregation))

        )))


    /**
      * This class creates the svg element for a pie chart.
      *
      * @param config the pie chart config
      * @param data the data
      *
      * @author Thomas Engel
     */
    case class PieChart(config: PieChartConfig, val data: ProcessedData*) extends Visualization(data: _*) {
        //val displayData = data.headOption.getOrElse(null)
        // TODO: for testing only!!!
        val displayData = new ProcessedData("aggregatedData", List(
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(2), Set(new AggregationTag("Wert 1"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(10), Set(new AggregationTag("Wert 2"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(50), Set(new AggregationTag("Wert 3"))),
            new TaggedDataPoint(new Timestamp(0), new DoubleValue(20), Set(new AggregationTag("Wert 4")))))

        require(displayData != null, "There are no data to display.")

        val radius = config._radius.getOrElse(calculateRadius)
        val innerRadiusPercent = config._innerRadiusPercent.getOrElse(0.0)
        val innerRadius = radius * innerRadiusPercent
        val labelRadius = radius * 1.05

        require(radius > 0, "Value for radius must be greater than 0.")
        require(innerRadius >= 0, "Negative value for innerRadius is not allowed.")

        /**
          * @return a list of important angles needed to calculate the radius
          */
        private def getAllAngleCandidates: List[Int] = {
            var angles = List(config.angleStart, config.angleEnd)
            if(Segment.isAngleContained(-270, config.angleStart, config.angleEnd)) angles = -270 :: angles
            if(Segment.isAngleContained(-180, config.angleStart, config.angleEnd)) angles = -180 :: angles
            if(Segment.isAngleContained(-90, config.angleStart, config.angleEnd)) angles = -90 :: angles
            if(Segment.isAngleContained(0, config.angleStart, config.angleEnd)) angles = 0 :: angles
            if(Segment.isAngleContained(90, config.angleStart, config.angleEnd)) angles = 90 :: angles
            if(Segment.isAngleContained(180, config.angleStart, config.angleEnd)) angles = 180 :: angles
            if(Segment.isAngleContained(270, config.angleStart, config.angleEnd)) angles = 270 :: angles

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

            config.chartWidth * (maxXRange / (xright - xleft))
        }

        /**
          * @return the maximal possible radius considering the y-dimension
          */
        private def getMaxRadiusY: Double = {
            val (ytop, ybottom) = calculateYRange
            val maxYRange = math.max(math.abs(ytop), math.abs(ybottom))

            config.chartHeight * (maxYRange / (ybottom - ytop))
        }

        /**
          * @return the maximal possible radius considering both dimensions
          */
        private def calculateRadius: Double = {
            math.min(getMaxRadiusX, getMaxRadiusY)
        }

        private def calculateOrigin: (Double, Double) = {
            val maxRx = getMaxRadiusX
            val maxRy = getMaxRadiusY

            val (x1, x2) = calculateXRange
            val dx = x2 - x1
            val xradStart = if(math.abs(x2) < math.abs(x1)) -1 else 1

            val (y1, y2) = calculateYRange
            val dy = y2 - y1
            val yradStart = if (math.abs(y2) < math.abs(y1)) -1 else 1

            val ox = (if(xradStart < 0) radius else - radius + config.chartWidth) - xradStart * (math.max(0, dx * (getMaxRadiusX - getMaxRadiusY))) / 2.0
            val oy = (if(yradStart < 0) radius else - radius + config.chartHeight) - yradStart * (math.max(0, dy * (getMaxRadiusY - getMaxRadiusX))) / 2.0

            (config.border.left + ox, config.border.top + oy)
        }

        /**
          * @return the difference between the start and end angle
          */
        private def getDeltaAngles: Double = {
            config.angleEnd - config.angleStart
        }

        /**
          * @return the total sum of all the values contained in the data
          */
        private def calculateSumValues: Double = {
            displayData.dataPoints.map((p: TaggedDataPoint) => p.value.value).sum
        }

        /**
          * @return a map containing the value for each datapoint
          */
        private def getRealValues: Map[String, String] = {
            displayData.dataPoints.map(
                (p: TaggedDataPoint) =>
                    p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.toString) (collection.breakOut): Map[String, String]
        }

        /**
          * @return a map containing the percentual value for each datapoint
          */
        private def calculatePercentValues: Map[String, Double] = {
            val total = calculateSumValues
            displayData.dataPoints.map(
                (p: TaggedDataPoint) =>
                    p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.value.value / total) (collection.breakOut): Map[String, Double]
        }

        private def getTags: Map[String, Set[Tag]] = {
            displayData.dataPoints.map(
                (p: TaggedDataPoint) =>
                    p.tags.filter((t: Tag) => t.isInstanceOf[AggregationTag]).head.toString -> p.tags) (collection.breakOut): Map[String, Set[Tag]]
        }

        /**
         * @return the calculated the list of [[Segment]]s
         */
        private def calculateSegments: List[Segment] = {
            var result: List[Segment] = List.empty

            var sAngle: Double = config.angleStart
            val deltaAngle = getDeltaAngles

            val percentMap = calculatePercentValues
            val valueMap = getRealValues
            val tagMap = getTags

            for(entry <- percentMap) {
                val dAngle = entry._2 * deltaAngle
                val start = sAngle
                sAngle += dAngle
                val end = sAngle

                val value = if(config.showValues) valueMap.get(entry._1).get else math.round(1000*entry._2)/10.0 + " %"
                val color = if(config.colorScheme.isDefined) Some(config.colorScheme.get.next) else None

                val segment = new Segment(
                    identifier = entry._1,
                    description = entry._1,
                    classes = tagMap.get(entry._1).get.map(_.toString),
                    angleStart = start,
                    angleEnd = end,
                    radius = radius,
                    innerRadius = innerRadius,
                    showValueLabel = config.showSegmentLabels,
                    valueRadius = labelRadius,
                    segmentLabelLineColor = config.segmentLabelLineColor,
                    value = value,
                    fontSize = config.fontSize,
                    color = color)

                result = segment :: result
            }

            result.reverse
        }

        def toSVG: Elem = {
            val (viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight) = config.viewBox
            val segments = calculateSegments
            val showLegend = config.legendPosition.isDefined


            var svg = (<g class="segments"
                transform={ s"translate$calculateOrigin" }>
                {for (seg <- segments) yield
                    seg.getSVGElement
                }
            </g>: SVGXML)
                .withTitle(
                    config.title,
                    config.width / 2,
                    config.border.top - config.titleVerticalOffset,
                    config.titleFontSize,
                    config.fontFamily)
                .withSVGHeader(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight)

            if(showLegend) {
                val (xlegend, ylegend) = config.getLegendCoordinates
                val legendLabels = segments.map((s: Segment) => Tuple2(s.description, s.color))

                svg = svg.withLegend(Legend(
                    xlegend,
                    ylegend,
                    config.legendSymbolWidth,
                    legendLabels,
                    config.fontSize,
                    config.fontFamily))
            }

            svg
        }
    }
}


/**
 * The config to create a [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]].
 *
 * @param ids ids set of ids which are to be displayed in the visualization
 * @param height the height
 * @param width the width
 * @param _border the border
 * @param _colorScheme the color scheme
 * @param _title the title
 * @param _titleVerticalOffset the vertical offset of the title
 * @param _titleFontSize the font size of the title
 * @param _fontSize the font size of labels
 * @param _fontFamily the font family of labels
 * @param _legendPosition the legend position
 * @param _legendHorizontalOffset the horizontal offset of the legend
 * @param _legendVerticalOffset the vertical offset of the legend
 * @param _legendSymbolWidth the width of the legend symbols
 * @param _showSegmentLabels if a label should be shown to a segment
 * @param _segmentLabelLineColor the color of the line connecting the label for a segment with the segment
 * @param _showValues if values should be shown to a segment
 * @param _angleStart the start angle
 * @param _angleEnd the end angle
 * @param _radius the radius
 * @param _innerRadiusPercent the inner radius that is cutted out in percent
 */
case class PieChartConfig(
    val ids:                        Seq[Identifier],
    val height:                     Int,
    val width:                      Int,
    val _border:                    Option[Border]     = None,
    val _colorScheme:               Option[String]  = None,
    val _title:                     Option[String]  = None,
    val _titleVerticalOffset:       Option[Int]     = None,
    val _titleFontSize:             Option[Int]     = None,
    val _fontSize:                  Option[Int]     = None,
    val _fontFamily:                Option[String]  = None,
    val _legendPosition:            Option[String]  = None,
    val _legendHorizontalOffset:    Option[Int]     = None,
    val _legendVerticalOffset:      Option[Int]     = None,
    val _legendSymbolWidth:         Option[Int]     = None,

    val _showSegmentLabels:         Option[Boolean] = None,
    val _segmentLabelLineColor:     Option[String]  = None,
    val _showValues:                Option[Boolean] = None,
    val _angleStart:                Option[Int]     = None,
    val _angleEnd:                  Option[Int]     = None,
    val _radius:                    Option[Double]  = None,
    val _innerRadiusPercent:        Option[Double]  = None
) extends VisualizationConfig(
    ids = ids,
    height = height,
    width = width,
    _border = _border,
    _colorScheme = _colorScheme,
    _title = _title,
    _titleVerticalOffset = _titleVerticalOffset,
    _titleFontSize = _titleFontSize,
    _fontSize = _fontSize,
    _fontFamily = _fontFamily,
    _legendPosition = _legendPosition,
    _legendHorizontalOffset = _legendHorizontalOffset,
    _legendVerticalOffset = _legendVerticalOffset,
    _legendSymbolWidth = _legendSymbolWidth) {

    /**
     * @return a default value for borderTop
     */
    override def borderTopDefault: Int = 100

    /**
     * @return a default value for borderRight
     */
    override def borderRightDefault: Int = 150

    /**
     * @return the default legend position
     */
    override def legendPositionDefault: Option[LegendPositionType.LegendPosition] = Some(LegendPositionType.RIGHT)

    /**
     * @return true iff a label should be shown for each segment
     */
    def showSegmentLabels: Boolean = _showSegmentLabels.getOrElse(true)

    /**
     * @return the color string for the line connecting the segment with the label
     */
    def segmentLabelLineColor: String = _segmentLabelLineColor.getOrElse("#000000")

    /**
      * @return true iff values should be shown to a segment
      */
    def showValues: Boolean = _showValues.getOrElse(false)

    /**
     * @return the start angle
     */
    def angleStart: Int = _angleStart.getOrElse(90) % 360

    /**
     * @return the end angle
     */
    def angleEnd: Int = _angleEnd.getOrElse(-270) % 360

    /**
      * Calculates the list of [[Segment]]s and returns the
      * [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]]
      * for this configuration.
      *
      * @param pool the pool containing the data
      * @return the [[threesixty.visualizer.visualizations.pieChart.PieChartConfig.PieChart]] for this configuration
     */
    def apply(pool: DataPool): PieChartConfig.PieChart = {
        PieChartConfig.PieChart(this, pool.getDatasets(ids: _*): _*)
    }

}
