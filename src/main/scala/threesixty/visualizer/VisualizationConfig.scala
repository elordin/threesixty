package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.Data.Identifier
import threesixty.processor.{ProcessingStep, ProcessingMethod}
import threesixty.visualizer.util.param._
import threesixty.visualizer.util.{ColorScheme, DefaultColorScheme, Legend}

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
 * @param _xAxis the x-axis
 * @param _yAxis the y-axis
 * @param _legend the legend
 */
abstract class VisualizationConfig(
    ids:                        Seq[Identifier],
    height:                     Int,
    width:                      Int,
    _border:                    Option[Border]          = None,
    _colorScheme:               Option[ColorScheme]     = None,
    _title:                     Option[OptTitleParam]   = None,
    _xAxis:                     Option[OptAxisParam]    = None,
    _yAxis:                     Option[OptAxisParam]    = None,
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
    val TITLE_POSITION_DEFAULT: PositionType.Position = PositionType.TOP
    val TITLE_VERTICAL_OFFSET_DEFAULT: Int = 20
    val TITLE_HORIZONTAL_OFFSET_DEFAULT: Int = 0
    val TITLE_SIZE_DEFAULT: Int = 20
    val TITLE_FONT_FAMILY_DEFAULT = "Roboto, Segoe UI"
    val TITLE_ALIGNMENT_DEFAULT: String = "middle"

    /**
     * @return the title position
     */
    private def titlePosition: PositionType.Position = {
        if(_title.isDefined) {
            PositionType.getPosition(_title.get.position.getOrElse("default")).getOrElse(TITLE_POSITION_DEFAULT)
        } else {
            TITLE_POSITION_DEFAULT
        }

    }

    /**
     * @return the title
     */
    def title: TitleParam = {
        if(_title.isDefined) {
            TitleParam(
                _title.get.title.getOrElse(TITLE_DEFAULT),
                titlePosition,
                _title.get.verticalOffset.getOrElse(TITLE_VERTICAL_OFFSET_DEFAULT),
                _title.get.horizontalOffset.getOrElse(TITLE_HORIZONTAL_OFFSET_DEFAULT),
                _title.get.size.getOrElse(TITLE_SIZE_DEFAULT),
                _title.get.fontFamily.getOrElse(TITLE_FONT_FAMILY_DEFAULT),
                _title.get.alignment.getOrElse(TITLE_ALIGNMENT_DEFAULT))
        } else {
            TitleParam(
                TITLE_DEFAULT,
                TITLE_POSITION_DEFAULT,
                TITLE_VERTICAL_OFFSET_DEFAULT,
                TITLE_HORIZONTAL_OFFSET_DEFAULT,
                TITLE_SIZE_DEFAULT,
                TITLE_FONT_FAMILY_DEFAULT,
                TITLE_ALIGNMENT_DEFAULT)
        }
    }

    /**
      * @return the coordinates of the top left corner of the legend
      */
    @throws[UnsupportedOperationException]("If titlePosition is not set or not knwon.")
    def getTitleCoordinates: (Int, Int) = {
        val (x, y) = titlePosition match {
            case PositionType.TOP => (width / 2, upperLimit)
            case PositionType.BOTTOM => (width / 2, height)
            case PositionType.LEFT => (border.left / 2, upperLimit)
            case PositionType.RIGHT => (rightLimit + border.right / 2, upperLimit)
            case _ => throw new UnsupportedOperationException("Title postion " + titlePosition.name + " is not supported.")
        }

        (x + title.horizontalOffset, y - title.verticalOffset)
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

    val X_AXIS_ARROW_SIZE_DEFAULT: Int = 10
    val Y_AXIS_ARROW_SIZE_DEFAULT: Int = 10

    val X_AXIS_ARROW_FILLED_DEFAULT: Boolean = false
    val Y_AXIS_ARROW_FILLED_DEFAULT: Boolean = false

    /**
     * @return the x-axis
     */
    def xAxis: AxisParam = {
        if(_xAxis.isDefined) {
            AxisParam(
                _xAxis.get.label.getOrElse(X_AXIS_LABEL_DEFAULT),
                _xAxis.get.labelSize.getOrElse(X_AXIS_LABEL_SIZE_DEFAULT),
                _xAxis.get.labelFontFamily.getOrElse(X_AXIS_LABEL_FONT_FAMILY_DEFAULT),
                _xAxis.get.minPxBetweenGridPoints.getOrElse(X_AXIS_MIN_DISTANCE_DEFAULT),
                _xAxis.get.unitLabelSize.getOrElse(X_AXIS_UNIT_LABEL_SIZE_DEFAULT),
                _xAxis.get.unitLabelFontFamily.getOrElse(X_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT),
                _xAxis.get.showGrid.getOrElse(X_AXIS_GRID_DEFAULT),
                _xAxis.get.showLabels.getOrElse(X_AXIS_LABELS_DEFAULT),
                _xAxis.get.arrowSize.getOrElse(X_AXIS_ARROW_SIZE_DEFAULT),
                _xAxis.get.arrowFilled.getOrElse(X_AXIS_ARROW_FILLED_DEFAULT))
        } else {
            AxisParam(
                X_AXIS_LABEL_DEFAULT,
                X_AXIS_LABEL_SIZE_DEFAULT,
                X_AXIS_LABEL_FONT_FAMILY_DEFAULT,
                X_AXIS_MIN_DISTANCE_DEFAULT,
                X_AXIS_UNIT_LABEL_SIZE_DEFAULT,
                X_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT,
                X_AXIS_GRID_DEFAULT,
                X_AXIS_LABELS_DEFAULT,
                X_AXIS_ARROW_SIZE_DEFAULT,
                X_AXIS_ARROW_FILLED_DEFAULT)
        }
    }

    /**
      * @return the y-axis
      */
    def yAxis: AxisParam = {
        if(_xAxis.isDefined) {
            AxisParam(
                _yAxis.get.label.getOrElse(Y_AXIS_LABEL_DEFAULT),
                _yAxis.get.labelSize.getOrElse(Y_AXIS_LABEL_SIZE_DEFAULT),
                _yAxis.get.labelFontFamily.getOrElse(Y_AXIS_LABEL_FONT_FAMILY_DEFAULT),
                _yAxis.get.minPxBetweenGridPoints.getOrElse(Y_AXIS_MIN_DISTANCE_DEFAULT),
                _yAxis.get.unitLabelSize.getOrElse(Y_AXIS_UNIT_LABEL_SIZE_DEFAULT),
                _yAxis.get.unitLabelFontFamily.getOrElse(Y_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT),
                _yAxis.get.showGrid.getOrElse(Y_AXIS_GRID_DEFAULT),
                _yAxis.get.showLabels.getOrElse(Y_AXIS_LABELS_DEFAULT),
                _yAxis.get.arrowSize.getOrElse(Y_AXIS_ARROW_SIZE_DEFAULT),
                _yAxis.get.arrowFilled.getOrElse(Y_AXIS_ARROW_FILLED_DEFAULT))
        } else {
            AxisParam(
                Y_AXIS_LABEL_DEFAULT,
                Y_AXIS_LABEL_SIZE_DEFAULT,
                Y_AXIS_LABEL_FONT_FAMILY_DEFAULT,
                Y_AXIS_MIN_DISTANCE_DEFAULT,
                Y_AXIS_UNIT_LABEL_SIZE_DEFAULT,
                Y_AXIS_UNIT_LABEL_FONT_FAMILY_DEFAULT,
                Y_AXIS_GRID_DEFAULT,
                Y_AXIS_LABELS_DEFAULT,
                Y_AXIS_ARROW_SIZE_DEFAULT,
                Y_AXIS_ARROW_FILLED_DEFAULT)
        }
    }

    val LEGEND_POSITION_DEFAULT: Option[PositionType.Position] = None
    val LEGEND_VERTICAL_OFFSET_DEFAULT: Int = 20
    val LEGEND_HORIZONTAL_OFFSET_DEFAULT: Int = 20
    val LEGEND_SYMBOL_WIDTH_DEFAULT: Int = 10
    val LEGEND_SIZE_DEFAULT: Int = 12
    val LEGEND_FONT_FAMILY_DEFAULT: String = "Roboto, Segoe UI"

    /**
     * @return the legend position
     */
    private def legendPosition: Option[PositionType.Position] = {
        if(_legend.isDefined) {
            PositionType.getPosition(_legend.get.position.get)
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
                case PositionType.TOP => (0, 0)
                case PositionType.BOTTOM => (0, lowerLimit)
                case PositionType.LEFT => (0, upperLimit)
                case PositionType.RIGHT => (rightLimit, upperLimit)
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
