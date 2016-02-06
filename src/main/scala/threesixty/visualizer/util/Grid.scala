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
    xPositions: Seq[Int],
    yPositions: Seq[Int],
    hDashed: Boolean = true,
    vDashed: Boolean = true,
    hDashArray: String = "5, 5",
    vDashArray: String = "5, 5"
) extends Renderable {

    def toSVG: Elem =
        <g class="grid">
            <g class="horizontal">
                {
                    for { currY <- yPositions } yield
                        <line
                            fill="none"
                            stroke="#AAAAAA"
                            stroke-dasharray={ if (hDashed) hDashArray else "0" }
                            x1={ (x + currY).toString }
                            y1={ currY.toString }
                            x2={ (x + width).toString }
                            y2={ currY.toString } />
                }
            </g>
            <g class="vertical">
                {
                    for { currX <- xPositions } yield
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
