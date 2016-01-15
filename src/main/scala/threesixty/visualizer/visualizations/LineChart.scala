package threesixty.visualizer.visualizations

import threesixty.data.ProcessedData
import threesixty.data.Data.{ValueType, Timestamp}
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
    def apply(json: String): LineChartConfig = new LineChartConfig(100, 200) // TODO actually read JSON


    case class LineChart(config: LineChartConfig) extends Visualization {
        def toSVG: Elem =
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
                <g id="data">
                    // TODO: Generate dynamically based on datapoints
                    <polyline fill="none" stroke="#cc0000" stroke-width="2px" points="127.5,500 256,450 384,475 512,350 640,375 768,300 896,325"/>
                    <g id="datapoints">
                        // TODO: Generate dynamically based on datapoints
                        <circle fill="#333333" stroke="#000000" cx="256" cy="450" r="4"/>
                        <circle fill="#333333" stroke="#000000" cx="384" cy="475" r="4"/>
                        <circle fill="#333333" stroke="#000000" cx="512" cy="350" r="4"/>
                        <circle fill="#333333" stroke="#000000" cx="640" cy="375" r="4"/>
                        <circle fill="#333333" stroke="#000000" cx="768" cy="300" r="4"/>
                    </g>
                </g>
                // TODO: Use text from config
                <text transform="matrix(1 0 0 1 468.8481 78.0879)" font-family="Robot, Segoe UI" font-weight="100" font-size="48">Title</text>
                <text transform="matrix(1 0 0 1 492.2236 708.6963)" font-family="Roboto, Segoe UI" font-size="16">x-Axis</text>
                <text transform="matrix(0 -1 1 0 68.6958 403.8643)" font-family="Roboto, Segoe UI" font-size="16">y-Axis</text>
            </svg>
    }
}


case class LineChartConfig private (
    height: Int,
    width: Int,
    xMin: Option[Timestamp] = None,
    xMax: Option[Timestamp] = None,
    yMin: Option[ValueType] = None,
    yMax: Option[ValueType] = None,
    xLabel: String = "",
    yLabel: String = "",
    title: String = ""
) extends VisualizationConfig {
    val metadata = new VisualizationMetadata(
            List(DataRequirement(scaling = Some(Scaling.Ordinal))), true)

    def apply(config: Config): LineChartConfig.LineChart = LineChartConfig.LineChart(this)

}
