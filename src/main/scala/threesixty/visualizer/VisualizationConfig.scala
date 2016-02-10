package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.Data.Identifier
import threesixty.processor.{ProcessingStep, ProcessingMethod}
import threesixty.visualizer.util._

/**
 * Generic Configuration for a [[threesixty.visualizer.Visualization]].
 * Acts as a factory for creating [[threesixty.visualizer.Visualization]]s.
 *
 * @param ids ids set of ids which are to be displayed in the visualization
 * @param height the height
 * @param width the width
 * @param _border the border
 * @param _colorScheme the color scheme
 * @param _title the title
 * @param _titleVerticalOffset the vertical offset of the title
 * @param _titleFontSize the font size of the title
 * @param _xLabel the x-axis label
 * @param _yLabel the y-axis label
 * @param _minPxBetweenXGridPoints the minimum distance in px between two grid points on the x-axis
 * @param _minPxBetweenYGridPoints the minimum distance in px between two grid points on the y-axis
 * @param _fontSize the font size of labels
 * @param _fontFamily the font family of labels
 * @param _legendPosition the legend position
 * @param _legendHorizontalOffset the horizontal offset of the legend
 * @param _legendVerticalOffset the vertical offset of the legend
 * @param _legendSymbolWidth the width of the legend symbols
 */
abstract class VisualizationConfig(
    ids:                        Seq[Identifier],
    height:                     Int,
    width:                      Int,
    _border:                    Option[OptBorder]   = None,
    _colorScheme:               Option[ColorScheme] = None,
    _title:                     Option[String]      = None,
    _titleVerticalOffset:       Option[Int]         = None,
    _titleFontSize:             Option[Int]         = None,
    _xLabel:                    Option[String]      = None,
    _yLabel:                    Option[String]      = None,
    _minPxBetweenXGridPoints:   Option[Int]         = None,
    _minPxBetweenYGridPoints:   Option[Int]         = None,
    _fontSize:                  Option[Int]         = None,
    _fontFamily:                Option[String]      = None,
    _legendPosition:            Option[String]      = None,
    _legendHorizontalOffset:    Option[Int]         = None,
    _legendVerticalOffset:      Option[Int]         = None,
    _legendSymbolWidth:         Option[Int]         = None
) extends Function1[DataPool, Visualization] {

    require(height > 0, "Value for height must be greater than 0.")
    require(width > 0, "Value for width must be greater than 0.")

    val BORDER_TOP_DEFAULT: Int = 100
    val BORDER_BOTTOM_DEFAULT: Int = 50
    val BORDER_LEFT_DEFAULT: Int = 50
    val BORDER_RIGHT_DEFAULT: Int = 50

    /**
     * @return the border
     */
    def border: Border = {
        if(_border.isDefined) {
            Border(
                top = _border.get.top.getOrElse(BORDER_TOP_DEFAULT),
                bottom = _border.get.bottom.getOrElse(BORDER_BOTTOM_DEFAULT),
                left = _border.get.left.getOrElse(BORDER_LEFT_DEFAULT),
                right = _border.get.right.getOrElse(BORDER_RIGHT_DEFAULT))
        } else {
            Border(
                top = BORDER_TOP_DEFAULT,
                bottom = BORDER_BOTTOM_DEFAULT,
                left = BORDER_LEFT_DEFAULT,
                right = BORDER_RIGHT_DEFAULT)
        }
    }

    /**
     * @return the color scheme
     */
    def colorScheme: ColorScheme = _colorScheme.getOrElse(DefaultColorScheme)

    /**
     * @return the default vertical offset of the title
     */
    def titleVerticalOffsetDefault: Int = 20

    /**
      * @return the default font size of the title
      */
    def titleFontSizeDefault: Int = 20

    /**
     * @return the title
     */
    def title: String = _title.getOrElse("")

    /**
     * @return the vertical offset of the title
     */
    def titleVerticalOffset: Int = _titleVerticalOffset.getOrElse(titleVerticalOffsetDefault)

    /**
     * @return the default font size of the title
     */
    def titleFontSize: Int = _titleFontSize.getOrElse(titleFontSizeDefault)

    require(titleFontSize > 0, "Value for the font size of the title must be positive.")

    /**
     * @return the label for the x-axis
     */
    def xLabel: String = _xLabel.getOrElse("")

    /**
     * @return the label for the y-axis
     */
    def yLabel: String = _yLabel.getOrElse("")

    /**
     * @return the default value for the minimum px between two grid points on the x-axis
     */
    def minPxBetweenXGridPointsDefault: Int = 20

    /**
     * @return the default value for the minimum px between two grid points on the y-axis
     */
    def minPxBetweenYGridPointsDefault: Int = 20

    /**
     * @return the minimum px between two grid points on the x-axis
     */
    def minPxBetweenXGridPoints: Int = _minPxBetweenXGridPoints.getOrElse(minPxBetweenXGridPointsDefault)

    require(minPxBetweenXGridPoints > 0, "Value for the minimum px between two grid points on the x-axis must be greater than 0.")

    /**
     * @return the minimum px between two grid points on the y-axis
     */
    def minPxBetweenYGridPoints: Int = _minPxBetweenYGridPoints.getOrElse(minPxBetweenYGridPointsDefault)

    require(minPxBetweenYGridPoints > 0, "Value for the minimum px between two grid points on the y-axis must be greater than 0.")

    /**
      * @return the default value for the font size
      */
    def fontSizeDefault: Int = 12

    /**
     * @return the default font family
     */
    def fontFamilyDefault: String = "Roboto, Segoe UI"

    /**
     * @return the font size
     */
    def fontSize: Int = _fontSize.getOrElse(fontSizeDefault)

    require(fontSize > 0, "Value for font size must be positive.")

    /**
     * @return the font family
     */
    def fontFamily: String = _fontFamily.getOrElse(fontFamilyDefault)

    /**
     * @return the default legend position
     */
    def legendPositionDefault: Option[LegendPositionType.LegendPosition] = None

    /**
     * @return the default horizontal offset of the legend
     */
    def legendHorizontalOffsetDefault: Int = 20

    /**
     * @return the default vertical offset of the legend
     */
    def legendVerticalOffsetDefault: Int = 20

    /**
     * @return the default legend symbol width
     */
    def legendSymbolWidthDefault: Int = 10

    /**
     * @return the legend position
     */
    def legendPosition: Option[LegendPositionType.LegendPosition] = {
        if(_legendPosition.isDefined) {
            Legend.getLegendPosition(_legendPosition.get)
        } else {
            legendPositionDefault
        }

    }

    /**
     * @return the horizontal offset of the legend
     */
    def legendHorizontalOffset: Int = _legendHorizontalOffset.getOrElse(legendHorizontalOffsetDefault)

    /**
     * @return the vertical offset of the legend
     */
    def legendVerticalOffset: Int = _legendVerticalOffset.getOrElse(legendVerticalOffsetDefault)

    /**
     * @return the width of the legend symbol
     */
    def legendSymbolWidth: Int = _legendSymbolWidth.getOrElse(legendSymbolWidthDefault)

    /**
     * @return the coordinates of the top left corner of the legend
     */
    @throws[UnsupportedOperationException]("If legendPosition is not set or not knwon.")
    def getLegendCoordinates: (Int, Int) = {
        val (x, y) = legendPosition
            .getOrElse(throw new UnsupportedOperationException("Cannot calculate legend position because no legend position was set.")) match {
                case LegendPositionType.TOP => (0, 0)
                case LegendPositionType.BOTTOM => (0, lowerLimit)
                case LegendPositionType.LEFT => (0, upperLimit)
                case LegendPositionType.RIGHT => (rightLimit, upperLimit)
                case _ => throw new UnsupportedOperationException("Legend postion " + legendPosition.get.name + " is not supported.")
        }

        (x + legendHorizontalOffset, y + legendVerticalOffset)
    }

    /**
     * @return the widht for the chart without the borders
     */
    def chartWidth: Int = width - border.left - border.right

    /**
     * @return the height for the chart without the borders
     */
    def chartHeight: Int = height - border.top - border.bottom

    require(chartWidth > 0, "The available width for the chart must be greater than 0.")
    require(chartHeight > 0, "The available height for the chart must be greater than 0.")

    /**
     * @return the viewbox for the svg
     */
    def viewBox: (Int, Int, Int, Int) = {
        (0, 0, width, height)
    }

    /**
     * @return the y-coordinate of the bottom of the chart
     */
    def lowerLimit = height - border.bottom

    /**
     * @return the y-coordinate of the top of the chart
     */
    def upperLimit = border.top

    /**
     * @return the x-coordinate of the left end of the chart
     */
    def leftLimit  = border.left

    /**
     * @return the x-coordinate of the right end of the chart
     */
    def rightLimit = width - border.right
}
