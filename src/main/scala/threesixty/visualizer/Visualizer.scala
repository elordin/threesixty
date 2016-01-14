package threesixty.visualizer

import threesixty.data.ProcessedData
import threesixty.config.Config

import spray.json._
import DefaultJsonProtocol._


case class VisualizationInfo(
    val name: String,
    val conversion: (String) => VisualizationConfig,
    val usage: String
)


trait VisualizationConfig extends Function1[Config, Visualization]


trait withVisualizationInfos {
    def visualizationInfos: Map[String, VisualizationInfo] = Map.empty
}


/**
 *  Visualizer allows to convert a definition of a desired output
 *  visualization into a fitting VisualizationConfig.
 *
 *  Stack traits derived from withVisualizationConversions to add
 *  further possible visualizations.
 *
 *  @author Thomas Weber
 *
 *  @example {{{
 *      val visualizer = new Visualizer with LineChartConfig.Conversion with HeatMapConfig.Conversion
 *  }}}
 */
class Visualizer extends withVisualizationInfos {

    @throws[NoSuchElementException]("if the json specifies a type that has no conversion")
    def toVisualizationConfig(jsonString: String): VisualizationConfig = {
        val json: JsObject = jsonString.parseJson.asJsObject
        val vizType = json.getFields("type")(0).convertTo[String]

        val conversion: (String) => VisualizationConfig =
            this.visualizationInfos.getOrElse(vizType,
                throw new DeserializationException(s"Unknown visualization type $vizType")
            ).conversion

        val args: String = ???    // get args from visualization

        conversion(args)
    }

}
