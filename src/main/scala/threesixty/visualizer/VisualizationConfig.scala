package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.Data.Identifier
import threesixty.processor.{ProcessingStep, ProcessingMethod}
import threesixty.visualizer.util.param._
import threesixty.visualizer.util.{LegendPositionType, ColorScheme, DefaultColorScheme, Legend}

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
 * @param _xLabel the x-axis label
 * @param _yLabel the y-axis label
 * @param _xAxisGrid if the grid for the x-axis should be shown
 * @param _yAxisGrid if the gird for the y-axis should be shown
 * @param _xAxisLabels if the labels for the x-axis should be shown
 * @param _yAxisLabels if the labels for the y-axis should be shown
 * @param _legend the legend
 */
abstract class VisualizationConfig(
    ids:                        Seq[Identifier],
    height:                     Int,
    width:                      Int,
    _border:                    Option[Border]          = None,
    _colorScheme:               Option[ColorScheme]     = None,
    _title:                     Option[OptTitleParam]   = None,
    _xLabel:                    Option[String]          = None,
    _yLabel:                    Option[String]          = None,
    _xLabelSize:                Option[Int]             = None,
    _yLabelSize:                Option[Int]             = None,
    _xLabelFontFamily:          Option[String]          = None,
    _yLabelFontFamily:          Option[String]          = None,
    _minPxBetweenXGridPoints:   Option[Int]             = None,
    _minPxBetweenYGridPoints:   Option[Int]             = None,
    _xUnitLabelSize:            Option[Int]             = None,
    _yUnitLabelSize:            Option[Int]             = None,
    _xUnitLabelFontFamily:      Option[String]          = None,
    _yUnitLabelFontFamily:      Option[String]          = None,
    _xAxisGrid:                 Option[Boolean]         = None,
    _yAxisGrid:                 Option[Boolean]         = None,
    _xAxisLabels:               Option[Boolean]         = None,
    _yAxisLabels:               Option[Boolean]         = None,
    _legend:                    Option[OptLegendParam]  = None
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
    def border:Border = _border.getOrElse(Border(
        top = BORDER_TOP_DEFAULT,
        bottom = BORDER_BOTTOM_DEFAULT,
        left = BORDER_LEFT_DEFAULT,
        right = BORDER_RIGHT_DEFAULT))

    /**
     * @return the color scheme
     */
    def colorScheme: ColorScheme = _colorScheme.getOrElse(DefaultColorScheme)

    val TITLE_DEFAULT: String = ""
    val TITLE_VERTICAL_OFFSET_DEFAULT: Int = 20
    val TITLE_HORIZONTAL_OFFSET_DEFAULT: Int = 0
    val TITLE_SIZE_DEFAULT: Int = 20
    val TITLE_FONT_FAMILY_DEFAULT = "Roboto, Segoe UI"
    val TITLE_ALIGNMENT_DEFAULT: String = "middle"

    /**
     * @return the title
     */
    def title: TitleParam = {
        if(_title.isDefined) {
            TitleParam(
                _title.get.title.getOrElse(TITLE_DEFAULT),
                _title.get.verticalOffset.getOrElse(TITLE_VERTICAL_OFFSET_DEFAULT),
                _title.get.horizontalOffset.getOrElse(TITLE_HORIZONTAL_OFFSET_DEFAULT),
                _title.get.size.getOrElse(TITLE_SIZE_DEFAULT),
                _title.get.fontFamily.getOrElse(TITLE_FONT_FAMILY_DEFAULT),
                _title.get.alignment.getOrElse(TITLE_ALIGNMENT_DEFAULT))
        } else {
            TitleParam(
                TITLE_DEFAULT,
                TITLE_VERTICAL_OFFSET_DEFAULT,
                TITLE_HORIZONTAL_OFFSET_DEFAULT,
                TITLE_SIZE_DEFAULT,
                TITLE_FONT_FAMILY_DEFAULT,
                TITLE_ALIGNMENT_DEFAULT)
        }
    }

    val X_AXIS_LABEL_DEFAULT : String = ""
    val Y_AXIS_LABEL_DEFAULT: String  = ""

    val X_AXIS_LABEL_SIZE_DEFAULT: Int = 12
    val Y_AXIS_LABEL_SIZE_DEFAULT: Int = 12

    val X_AXIS_LABEL_FONT_FAMILY_DEFAULT: String = "Roboto, Segoe UI"
    val Y_AXIS_LABEL_FONT_FAMILY_DEFAULT: String = "Roboto, Segoe UI"

    val X_AXIS_MIN_DISTANCE_DEFAULT: Int = 20
    val Y_AXIS_MIN_DISTANCE_DEFAULT: Int = 20

    val X_AXIS_UNIT_LABEL_SIZE_DEFAULT: Int = 12
    val Y_AXIS_UNIT_LABEL_SIZE_DEFAULT: Int = 12

    val X_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT: String = "Roboto, Segoe UI"
    val Y_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT: String = "Roboto, Segoe UI"

    val X_AXIS_GRID_DEFAULT: Boolean = true
    val Y_AXIS_GRID_DEFAULT: Boolean = true

    val X_AXIS_LABELS_DEFAULT: Boolean = true
    val Y_AXIS_LABELS_DEFAULT: Boolean = true

    /**
     * @return the label for the x-axis
     */
    def xLabel: String = _xLabel.getOrElse(X_AXIS_LABEL_DEFAULT)

    /**
     * @return the label for the y-axis
     */
    def yLabel: String = _yLabel.getOrElse(Y_AXIS_LABEL_DEFAULT)

    /**
     * @return the label size for the x-axis
     */
    def xLabelSize: Int = _xLabelSize.getOrElse(X_AXIS_LABEL_SIZE_DEFAULT)

    /**
     * @return the label size for the y-axis
     */
    def yLabelSize: Int = _yLabelSize.getOrElse(Y_AXIS_LABEL_SIZE_DEFAULT)

    /**
     * @return the font family of the label for the x-axis
     */
    def xLabelFontFamily: String = _xLabelFontFamily.getOrElse(X_AXIS_LABEL_FONT_FAMILY_DEFAULT)

    /**
     * @return the font family of the label for the y-axis
     */
    def yLabelFontFamily: String = _yLabelFontFamily.getOrElse(Y_AXIS_LABEL_FONT_FAMILY_DEFAULT)

    /**
     * @return the minimum px between two grid points on the x-axis
     */
    def minPxBetweenXGridPoints: Int = _minPxBetweenXGridPoints.getOrElse(X_AXIS_MIN_DISTANCE_DEFAULT)

    require(minPxBetweenXGridPoints > 0, "Value for the minimum px between two grid points on the x-axis must be greater than 0.")

    /**
     * @return the minimum px between two grid points on the y-axis
     */
    def minPxBetweenYGridPoints: Int = _minPxBetweenYGridPoints.getOrElse(Y_AXIS_MIN_DISTANCE_DEFAULT)

    require(minPxBetweenYGridPoints > 0, "Value for the minimum px between two grid points on the y-axis must be greater than 0.")

    /**
     * @return the label size of the unit labels for the x-axis
     */
    def xUnitLabelSize: Int = _xUnitLabelSize.getOrElse(X_AXIS_UNIT_LABEL_SIZE_DEFAULT)

    /**
     * @return the label size of the unit labels for the y-axis
     */
    def yUnitLabelSize: Int = _yUnitLabelSize.getOrElse(Y_AXIS_UNIT_LABEL_SIZE_DEFAULT)

    /**
      * @return the font family of the label for the x-axis
      */
    def xUnitLabelFontFamily: String = _xUnitLabelFontFamily.getOrElse(X_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT)

    /**
      * @return the font family of the label for the y-axis
      */
    def yUnitLabelFontFamily: String = _yUnitLabelFontFamily.getOrElse(Y_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT)

    /**
     * @return true iff the grid for the x-axis should be shown
     */
    def xAxisGrid: Boolean = _xAxisGrid.getOrElse(X_AXIS_GRID_DEFAULT)

    /**
     * @return true iff the grid for the y-axis should be shown
     */
    def yAxisGrid: Boolean = _yAxisGrid.getOrElse(Y_AXIS_GRID_DEFAULT)

    /**
     * @return true iff the labels for the x-axis should be shown
     */
    def xAxisLabels: Boolean = _xAxisLabels.getOrElse(X_AXIS_LABELS_DEFAULT)

    /**
     * @return true iff the labels for the y-axis should be shown
     */
    def yAxisLabels: Boolean = _yAxisLabels.getOrElse(Y_AXIS_LABELS_DEFAULT)

    val LEGEND_POSITION_DEFAULT: Option[LegendPositionType.LegendPosition] = None
    val LEGEND_VERTICAL_OFFSET_DEFAULT: Int = 20
    val LEGEND_HORIZONTAL_OFFSET_DEFAULT: Int = 20
    val LEGEND_SYMBOL_WIDTH_DEFAULT: Int = 10
    val LEGEND_SIZE_DEFAULT: Int = 12
    val LEGEND_FONT_FAMILY_DEFAULT: String = "Roboto, Segoe UI"

    /**
     * @return the legend position
     */
    private def legendPosition: Option[LegendPositionType.LegendPosition] = {
        if(_legend.isDefined) {
            Legend.getLegendPosition(_legend.get.position.get)
        } else {
            LEGEND_POSITION_DEFAULT
        }

    }

    /**
     * @return the legend
     */
    def legend: LegendParam = {
        if(_legend.isDefined) {
            LegendParam(
                legendPosition,
                _legend.get.verticalOffset.getOrElse(LEGEND_VERTICAL_OFFSET_DEFAULT),
                _legend.get.horizontalOffset.getOrElse(LEGEND_HORIZONTAL_OFFSET_DEFAULT),
                _legend.get.symbolWidth.getOrElse(LEGEND_SYMBOL_WIDTH_DEFAULT),
                _legend.get.size.getOrElse(LEGEND_SIZE_DEFAULT),
                _legend.get.fontFamily.getOrElse(LEGEND_FONT_FAMILY_DEFAULT))
        } else {
            LegendParam(
                LEGEND_POSITION_DEFAULT,
                LEGEND_VERTICAL_OFFSET_DEFAULT,
                LEGEND_HORIZONTAL_OFFSET_DEFAULT,
                LEGEND_SYMBOL_WIDTH_DEFAULT,
                LEGEND_SIZE_DEFAULT,
                LEGEND_FONT_FAMILY_DEFAULT)
        }
    }

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

        (x + legend.horizontalOffset, y + legend.verticalOffset)
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
