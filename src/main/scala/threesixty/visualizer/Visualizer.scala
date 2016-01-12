package threesixty.visualizer

import threesixty.data.ProcessedData


trait VisualizationConfig extends Function1[Set[ProcessedData], Visualization] {}


trait withVisualizationConversions {
    def visualizationConversions: Map[String, (String) => VisualizationConfig] = Map.empty
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
class Visualizer extends withVisualizationConversions {

    @throws[NoSuchElementException]("if the json specifies a type that has no conversion")
    def toVisualizationConfig(json: String): VisualizationConfig = {
        val vizType: String = ??? // get type from visualization
        val args: String = ???    // get args from visualization
        val conversion = visualizationConversions.getOrElse(vizType,
            throw new NoSuchElementException(s"No conversion for type $vizType found."))
        conversion(args)
    }

}
