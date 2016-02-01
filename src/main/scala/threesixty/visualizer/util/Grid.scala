package threesixty.visualizer.util

import threesixty.visualizer.Renderable

import scala.xml.Elem

/**
 *  @author Thomas Engel, Thomas Weber
 */
object Grid {

    implicit def toXML(grid: Grid): Elem = grid.toSVG

}

case class Grid(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    xGapSize: Int,
    yGapSize: Int,
    xOffset: Int = 0,
    yOffset: Int = 0,
    hDashed: Boolean = true,
    vDashed: Boolean = true,
    hDashArray: String = "5, 5",
    vDashArray: String = "5, 5"
) extends Renderable {
    require(xGapSize > 0, "Grid horizontal gap must be greater than zero.")
    require(yGapSize > 0, "Grid vertical gap must be greater than zero.")

    def toSVG: Elem =
        <g class="grid">
            <g class="horizontal">
                {
                    for { currY <- Range(y + yOffset, y - height, -1 * xGapSize).inclusive } yield
                        <line
                            fill="none"
                            stroke="#AAAAAA"
                            stroke-dasharray={ if (hDashed) hDashArray else "0" }
                            x1={ x.toString }
                            y1={ currY.toString }
                            x2={ (x + width).toString }
                            y2={ currY.toString } />
                }
            </g>
            <g class="vertical">
                {
                    for { currX <- Range(x + xOffset, width, yGapSize).inclusive } yield
                        <line
                            fill="none"
                            stroke="#AAAAAA"
                            stroke-dasharray={ if (hDashed) hDashArray else "0" }
                            x1={ currX.toString }
                            y1={ y.toString }
                            x2={ currX.toString }
                            y2={ (y - height).toString }/>
                }
            </g>
        </g>
}
