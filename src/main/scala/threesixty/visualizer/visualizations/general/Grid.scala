package threesixty.visualizer.visualizations.general

import scala.xml.Elem

/**
  * @author Thomas Engel
  */
case class Grid(val xAxis: Axis, val yAxis: Axis, val fontSize: Int = 12) {
    require(xAxis != null, "Null value for xAxis is not allowed.")
    require(yAxis != null, "Null value for yAxis is not allowed.")

    def convertPoint(x: Double, y: Double): (Double, Double) = {
        (xAxis.convertValue(x), - yAxis.convertValue(y))
    }

    def getSVGElement: Elem = {
        val left = xAxis.convertValue(xAxis.getMinimumDisplayedValue)
        val right = xAxis.convertValue(xAxis.getMaximumDisplayedValue)
        val top = yAxis.convertValue(yAxis.getMaximumDisplayedValue)
        val bottom = yAxis.convertValue(yAxis.getMinimumDisplayedValue)

        <g id="grid">
            <g id="xAxis">
                {for (d <- xAxis.getGridPointsAndLabel) yield
                    <line fill="none"
                          stroke={if(d._1 == 0) "#000000" else "#AAAAAA"}
                          stroke-dasharray={if (d._1 == 0) "0,0" else "5,5"}
                          x1={xAxis.convertValue(d._1).toString}
                          y1={bottom.toString}
                          x2={xAxis.convertValue(d._1).toString}
                          y2={top.toString}/>
                    <text x={left.toString}
                          y={(bottom + fontSize + 5).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="start">{d._2}</text>
                }
                <g id="xLabel">
                    <text x={(right + 20).toString}
                          y={(bottom - fontSize / 2.0).toString}
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
                          y1={yAxis.convertValue(d._1).toString}
                          x2={right.toString}
                          y2={yAxis.convertValue(d._1).toString}/>
                    <text x={(left - 10).toString}
                          y={(yAxis.convertValue(d._1) + fontSize / 2.0).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="end">{d._2}</text>
                }
                <g id="yLabel">
                    <text x={left.toString}
                          y={(top - 20).toString}
                          font-family="Roboto, Segoe UI"
                          font-weight="100"
                          font-size={fontSize.toString}
                          text-anchor="middle">{yAxis.getAxisLabel}</text>
                </g>
            </g>
        </g>
    }
}
