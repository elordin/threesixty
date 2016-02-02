package threesixty.visualizer

import threesixty.data.{InputData, DataPool}
import threesixty.data.Data.Identifier
import threesixty.processor.{ProcessingStep, ProcessingMethod}

/**
 * Generic Configuration for a [[threesixty.visualizer.Visualization]].
 * Acts as a factory for creating [[threesixty.visualizer.Visualization]]s.
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
 * @param fontSizeTitle the font size of the title
 * @param fontSize the font size of labels
 */
abstract class VisualizationConfig(
    ids: Seq[Identifier],
    height: Int,
    width: Int,
    _title: Option[String] = None,
    _borderTop: Option[Int] = None,
    _borderBottom: Option[Int] = None,
    _borderLeft: Option[Int] = None,
    _borderRight: Option[Int] = None,
    _distanceTitle: Option[Int] = None,
    _fontSizeTitle: Option[Int] = None,
    _fontSize: Option[Int] = None
) extends Function1[DataPool, Visualization] {

    /**
      * @return the width
      */
    def _width: Int = width

    /**
      * @return the height
      */
    def _height: Int = height

    require(height > 0, "Value for height must be greater than 0.")
    require(width > 0, "Value for width must be greater than 0.")

    def title: String = _title.getOrElse("")

    /**
      * @return a default value for borderTop
      */
    def borderTopDefault: Int = 100

    /**
      * @return a default value for borderBottom
      */
    def borderBottomDefault: Int = 50

    /**
      * @return a default value for borderLeft
      */
    def borderLeftDefault: Int = 50

    /**
      * @return a default value for borderRight
      */
    def borderRightDefault: Int = 50

    /**
      * @return a default value for distanceTitle
      */
    def distanceTitleDefault: Int = 10

    def borderTop: Int = _borderTop.getOrElse(borderTopDefault)
    def borderBottom: Int = _borderBottom.getOrElse(borderBottomDefault)
    def borderLeft: Int = _borderLeft.getOrElse(borderLeftDefault)
    def borderRight: Int = _borderRight.getOrElse(borderRightDefault)
    def distanceTitle: Int = _distanceTitle.getOrElse(distanceTitleDefault)

    require(borderTop >= 0, "Negative value for borderTop is not allowed.")
    require(borderBottom >= 0, "Negative value for borderBottom is not allowed.")
    require(borderLeft >= 0, "Negative value for borderLeft is not allowed.")
    require(borderRight >= 0, "Negative value for borderRight is not allowed.")

    /**
      * @return a default value for the fontSize
      */
    def fontSizeDefault: Int = 12

    /**
      * @return a default value for the fontSizeTitle
      */
    def fontSizeTitleDefault: Int = 20

    def fontSize: Int = _fontSize.getOrElse(fontSizeDefault)
    def fontSizeTitle: Int = _fontSizeTitle.getOrElse(fontSizeTitleDefault)

    require(fontSize > 0, "Value for font size must be positive.")
    require(fontSizeTitle > 0, "Value for font size title must be positive.")

    // calculate the available height and width for the chart
    def chartWidth: Int = width - borderLeft - borderRight
    def chartHeight: Int = height - borderTop - borderBottom

    require(chartWidth > 0, "The available width for the chart must be greater than 0.")
    require(chartHeight > 0, "The available height for the chart must be greater than 0.")

    def viewBox: (Int, Int, Int, Int) = {
        (-borderLeft, -borderBottom, width + borderRight, height + borderTop)
    }

    def lowerLimit = height - borderBottom
    def upperLimit = borderTop
    def leftLimit  = borderLeft
    def rightLimit = width - borderRight
}
