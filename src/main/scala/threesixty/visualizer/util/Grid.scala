package threesixty.visualizer.util

import threesixty.visualizer.Renderable

import scala.xml.Elem

/**
 *  @author Thomas Engel, Thomas Weber
 */
object Grid {

    implicit def toXML(grid: Grid): Elem = grid.toSVG

}

/**
 *  Pluggable
 */
case class Grid(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    hDivision: Int,
    vDivision: Int,
    hDashed: Boolean = true,
    vDashed: Boolean = true,
    hDashArray: String = "5, 5",
    vDashArray: String = "5, 5"
) extends Renderable {
    def toSVG: Elem =
        <g class="grid">
            <g class="horizontal">
                {
                    val vGapSize = height / vDivision
                    for { currY <- Range(y - height, y, vGapSize).inclusive } yield
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
                    val hGapSize = width / hDivision
                    for { currX <- Range(0, width, hGapSize).inclusive } yield
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
