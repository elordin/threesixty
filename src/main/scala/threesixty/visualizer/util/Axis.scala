package threesixty.visualizer.util

import threesixty.visualizer.Renderable

import scala.xml.Elem

/**
 *  @author Thomas Engel, Thomas Weber
 */
object Axis {
    implicit def toXML(axis: Axis): Elem = axis.toSVG
}

trait Axis extends Renderable


case class HorizontalAxis(
    x: Int,
    y: Int,
    width: Int,
    leftToRight: Boolean = true,
    arrowSize: Int = 10,
    arrowFilled: Boolean = false,
    strokeWidth: Int = 2,
    strokeColor: RGBColor = RGBColor.BLACK,
    title: String = "",
    xTitle: Option[Int] = None,
    yTitle: Option[Int] = None,
    titleSize: Int = 16,
    titleColor: RGBColor = RGBColor.BLACK,
    labels: Seq[(String, Int)] = Seq(),
    labelSize: Int = 12,
    labelColor: RGBColor = RGBColor.BLACK,
    labelRotation: Int = 0,
    labelOffset: Option[Int] = None
) extends Axis {

    def toSVG: Elem =
        <g class="axis horizontal">
            <line class="axis-line"
                x1={ x.toString }
                y1={ y.toString }
                x2={ (x + width).toString }
                y2={ y.toString }
                fill="none"
                stroke-width={ strokeWidth.toString }
                stroke={ strokeColor.toString } />
            <g class="arrowhead">
                <path
                    d={
                        if (leftToRight) {
                            s"M${x + width - arrowSize} ${y - arrowSize} L${x + width} $y L${x + width - arrowSize} ${y + arrowSize}"
                        } else {
                            s"M${x + arrowSize} ${y - arrowSize} L$x $y L${x + arrowSize} ${y + arrowSize}"
                        }
                    }
                    fill={ if (arrowFilled) strokeColor.toString else "none" }
                    stroke-width={ strokeWidth.toString }
                    stroke={ strokeColor.toString } />
            </g>
            {

                for { ((labelText, xOffset), i) <- labels.zipWithIndex } yield {
                    val labelX = x + xOffset
                    val labelY = y + labelOffset.getOrElse(2 * labelSize)
                    <text class="label"
                        x={ labelX.toString }
                        y={ labelY.toString }
                        transform={ s"rotate($labelRotation, $labelX, $labelY)"}
                        fill={ labelColor.toString }
                        font-size={ labelSize.toString }
                        text-anchor="middle">
                        { labelText }
                    </text>
                    <line class="label-dash"
                        x1={ labelX.toString }
                        y1={ y.toString }
                        x2={ labelX.toString }
                        y2={ (y + labelOffset.map(_/2).getOrElse(labelSize)).toString }
                        fill="none"
                        stroke-width={ strokeWidth.toString }
                        stroke={ strokeColor.toString }

                        />
                }
            }
            {
                if (title != "") {
                    <text class="axis-title"
                        x={ xTitle.getOrElse(x + width / 2).toString }
                        y={ yTitle.getOrElse(y + 3 * labelSize).toString }
                        fill={ titleColor.toString }
                        font-size={ titleSize.toString }
                        text-anchor="middle">
                        { title }
                    </text>
                }
            }
        </g>
}


case class VerticalAxis(
    x: Int,
    y: Int,
    height: Int,
    bottomToTop: Boolean = true,
    arrowSize: Int = 10,
    arrowFilled: Boolean = false,
    strokeWidth: Int = 2,
    strokeColor: RGBColor = RGBColor.BLACK,
    title: String = "",
    xTitle: Option[Int] = None,
    yTitle: Option[Int] = None,
    titleSize: Int = 16,
    titleColor: RGBColor = RGBColor.BLACK,
    labels: Seq[(String, Int)] = Seq(),
    labelSize: Int = 12,
    labelColor: RGBColor = RGBColor.BLACK,
    labelRotation: Int = 0,
    labelOffset: Option[Int] = None
) extends Axis {

    def toSVG: Elem =
        <g class="axis vertical">
            <line class="axis-line"
                x1={ x.toString }
                y1={ y.toString }
                x2={ x.toString }
                y2={ (y - height).toString }
                fill="none"
                stroke-width={ strokeWidth.toString }
                stroke={ strokeColor.toString } />
            <g class="arrowhead">
                <path
                    d={
                        if (bottomToTop) {
                            s"M${x - arrowSize} ${y + arrowSize - height} L$x ${y - height} L${x + arrowSize} ${y + arrowSize - height}"
                        } else {
                            s"M${x - arrowSize} ${y - arrowSize} L$x $y L${x + arrowSize} ${y - arrowSize}"
                        }
                    }
                    fill={ if (arrowFilled) strokeColor.toString else "none" }
                    stroke-width={ strokeWidth.toString }
                    stroke={ strokeColor.toString } />
            </g>
            {

                for { ((labelText, yOffset), i) <- labels.zipWithIndex } yield {
                    val labelX = x - labelOffset.getOrElse(2 * labelSize)
                    val labelY = y - yOffset
                    <text class="label"
                        x={ labelX.toString }
                        y={ labelY.toString }
                        transform={ s"rotate($labelRotation, $labelX, $labelY)"}
                        fill={ labelColor.toString }
                        font-size={ labelSize.toString }
                        text-anchor="end">
                        { labelText }
                    </text>
                    <line class="label-dash"
                        x1={ x.toString }
                        y1={ labelY.toString }
                        x2={ (x - labelOffset.map(_/2).getOrElse(labelSize)).toString }
                        y2={ labelY.toString }
                        fill="none"
                        stroke-width={ strokeWidth.toString }
                        stroke={ strokeColor.toString }

                        />
                }
            }
            {
                if (title != "") {
                    var titleX = xTitle.getOrElse(x - 3 * labelSize)
                    var titleY = yTitle.getOrElse(y - height / 2)
                    <text class="axis-title"
                        x={ titleX.toString }
                        y={ titleY.toString }
                        fill={ titleColor.toString }
                        font-size={ titleSize.toString }
                        transform={ s"rotate(-90, $titleX, $titleY)"}
                        text-anchor="middle">
                        { title }
                    </text>
                }
            }
        </g>

}
