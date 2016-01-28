package threesixty.visualizer.visualizations.general

import scala.xml.Elem

/**
 *  @author Thomas Engel
 */
case class Grid(val xAxis: Axis, val yAxis: Axis, val fontSize: Int = 12) {
    require(xAxis != null, "Null value for xAxis is not allowed.")
    require(yAxis != null, "Null value for yAxis is not allowed.")

    def convertPoint(x: Double, y: Double): (Double, Double) = {
        (xAxis.convert(x), yAxis.convert(y))
    }

    def getSVGElement: Elem = {
        val left = xAxis.convert(xAxis.getMinimumDisplayedValue)
        val right = xAxis.convert(xAxis.getMaximumDisplayedValue)
        val top = yAxis.convert(yAxis.getMaximumDisplayedValue)
        val bottom = yAxis.convert(yAxis.getMinimumDisplayedValue)

        var yCoordXLabel = bottom

        // if a x-axis is drawn place the label next to it
        val converted0 = yAxis.convert(0)
        if(bottom <= yCoordXLabel && yCoordXLabel <= top) {
           yCoordXLabel = 0
        }

        <g id="grid">
            <g id="xAxis">
                {for (d <- xAxis.getGridPointsAndLabel) yield
                    <line fill="none"
                          stroke={if(d._1 == 0) "#000000" else "#AAAAAA"}
                          stroke-dasharray={if (d._1 == 0) "0,0" else "5,5"}
                          x1={d._1.toString}
                          y1={bottom.toString}
                          x2={d._1.toString}
                          y2={top.toString}/>
                    <text x={d._1.toString}
                          y={(bottom + fontSize + 5).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="middle">{d._2}</text>
                }
                <g id="xLabel">
                    <text x={(right + 20).toString}
                          y={(yCoordXLabel + fontSize / 3.0).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="start">{xAxis.getAxisLabel}</text>
                </g>
            </g>
            <g id="yAxis">
                {for (d <- yAxis.getGridPointsAndLabel) yield
                    <line fill="none"
                          stroke={if(d._1 == 0) "#000000" else "#AAAAAA"}
                          stroke-dasharray={if (d._1 == 0) "0,0" else "5,5"}
                          x1={left.toString}
                          y1={d._1.toString}
                          x2={right.toString}
                          y2={d._1.toString}/>
                    <text x={(left - 10).toString}
                          y={(d._1 + fontSize / 3).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="end">{d._2}</text>
                }
                <g id="yLabel">
                    <text x={left.toString}
                          y={(top - 15).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="middle">{yAxis.getAxisLabel}</text>
                </g>
            </g>
        </g>
    }
}
