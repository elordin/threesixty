package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp, Identifier}
import threesixty.visualizer.{
        Visualization,
        VisualizationConfig,
        withVisualizationInfos,
        VisualizationInfo,
        DataRequirement,
        VisualizationMetadata
    }
import threesixty.data.metadata.Scaling
import threesixty.config.Config
import scala.xml.Elem


object LineChartConfig {
    trait Info extends withVisualizationInfos {
        abstract override def visualizationInfos: Map[String, VisualizationInfo] =
            super.visualizationInfos + ("linechart" ->
                VisualizationInfo(
                    "LineChart",
                    { json:String => LineChartConfig.apply(json) },
                    "Parameters: \n" +
                    "    height: Int    - Height of the diagram in px\n" +
                    "    width:  Int    - Width of the diagram in px\n" +
                    "    xMin:   Int    - Minimum value of the x-axis\n" +
                    "    xMax:   Int    - Maximum value of the x-axis\n" +
                    "    yMin:   Int    - Minimum value of the y-axis\n" +
                    "    yMax:   Int    - Maximum value of the y-axis\n" +
                    "    xLabel: String - Label for the x-axis\n" +
                    "    yLabel: String - Label for the y-axis\n" +
                    "    title:  String - Diagram title\n"
                )
            )
    }


    /**
     *  Public constructor that parses JSON into a configuration
     *  @param json representation of the config
     *  @return LineChartConfig with all arguments from the JSON set
     */
    def apply(json: String): LineChartConfig = new LineChartConfig(Set("data1i", "data2i", "data3i"), 768, 1024, title="Test Chart") // TODO actually read JSON


    case class LineChart(config: LineChartConfig, val data: Set[ProcessedData]) extends Visualization(data: Set[ProcessedData]) {
        def toSVG: Elem = {
            val width      = config.width * 0.8
            val leftOffset = config.width * 0.1
            val height     = config.height * 0.7
            val lowerBound = config.height * 0.85

            val xMin = config.xMin match {
                case None => data.map(_.data.map(_.timestamp.getTime).min).min
                case Some(v) => v.getTime
            }
            val xMax = config.xMax match {
                case None => data.map(_.data.map(_.timestamp.getTime).max).max
                case Some(v) => v.getTime
            }
            val yMin = config.yMin match {
                case None => data.map(_.data.map(_.value.value).min).min
                case Some(v) => v.value
            }
            val yMax = config.yMax match {
                case None => data.map(_.data.map(_.value.value).max).max
                case Some(v) => v.value
            }

            val stepX =  width / (xMax - xMin)
            val stepY =  height / (yMax - yMin)

            // TODO: Base size on config
            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 768" xml:space="preserve">
                <g id="grid">
                    // background-grid
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="512.5" x2="896" y2="512.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="576.5" x2="896" y2="576.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="448.5" x2="896" y2="448.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="384.5" x2="896" y2="384.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="320.5" x2="896" y2="320.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="256.5" x2="896" y2="256.5"/>
                    <line fill="none" stroke="#CCCCCC" x1="128" y1="192.5" x2="896" y2="192.5"/>
                </g>
                <line id="X-Axis" fill="none" stroke="#000000" x1="128" y1="640.5" x2="896" y2="640.5"/>
                <line id="Y-Axis" fill="none" stroke="#000000" x1="127.5" y1="641" x2="127.5" y2="129"/>
                <g id="x-ArrowHead">
                    <line fill="none" stroke="#000000" x1="896" y1="640.5" x2="884.687" y2="629.187"/>
                    <line fill="none" stroke="#000000" x1="896" y1="640.5" x2="884.687" y2="651.813"/>
                </g>
                <g id="y-ArrowHead">
                    <line fill="none" stroke="#000000" x1="127.5" y1="129" x2="116.187" y2="140.313"/>
                    <line fill="none" stroke="#000000" x1="127.5" y1="129" x2="138.813" y2="140.313"/>
                </g>
                <g id="yValueIndicators">
                    // TODO: Generate dynamically based on axis range and splitting
                    <line fill="none" stroke="#000000" x1="120" y1="512.5" x2="128" y2="512.5"/>
                    <line fill="none" stroke="#000000" x1="128" y1="576.5" x2="120" y2="576.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="448.5" x2="128" y2="448.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="384.5" x2="128" y2="384.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="320.5" x2="128" y2="320.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="256.5" x2="128" y2="256.5"/>
                    <line fill="none" stroke="#000000" x1="120" y1="192.5" x2="128" y2="192.5"/>
                </g>

                // TODO: Generate dynamically based on datapoints
                { for (dataset <- data) yield
                    <g id={dataset.id}>
                        // TODO: Line coordinates, color etc.
                        <polyline fill="none" stroke="#cc0000" stroke-width="2px" points={
                            (for (datapoint <- dataset.data) yield {
                                ((datapoint.timestamp.getTime - xMin) * stepX + leftOffset).toString + "," +
                                (lowerBound - (datapoint.value.value - yMin) * stepY).toString + " "}).fold("")(_ + _) } />
                        <g id="datapoints">
                            { for (datapoint <- dataset.data) yield
                                <circle fill="#333333" stroke="#000000" cx={ ((datapoint.timestamp.getTime - xMin) * stepX + leftOffset).toString } cy={ (lowerBound - (datapoint.value.value - yMin) * stepY).toString } r="4"/>
                            }
                        </g>
                    </g>
                }
                <text transform="matrix(1 0 0 1 468.8481 78.0879)" font-family="Roboto, Segoe UI" font-weight="100" font-size="48">{ config.title }</text>
                <text transform="matrix(1 0 0 1 492.2236 708.6963)" font-family="Roboto, Segoe UI" font-size="16">{ config.xLabel }</text>
                <text transform="matrix(0 -1 1 0 68.6958 403.8643)" font-family="Roboto, Segoe UI" font-size="16">{ config.yLabel }</text>
            </svg>
        }
    }
}


case class LineChartConfig(
    val ids: Set[Identifier],
    val height: Int,
    val width: Int,
    val xMin: Option[Timestamp] = None,
    val xMax: Option[Timestamp] = None,
    val yMin: Option[ValueType] = None,
    val yMax: Option[ValueType] = None,
    val xLabel: String = "",
    val yLabel: String = "",
    val title: String = ""
) extends VisualizationConfig(ids: Set[Identifier]) {
    val metadata = new VisualizationMetadata(
            List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    def apply(config: Config): LineChartConfig.LineChart = LineChartConfig.LineChart(this, config.getDatasets(ids))

}
