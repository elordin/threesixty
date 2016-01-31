package threesixty.visualizer

import threesixty.data.ProcessedData
import scala.xml.Elem


/**
 *  Generic visualization.
 *
 *  If they require additional parameters, use
 *  [[threesixty.visualizer.VisualizationConfig]] to store those parameters
 *  and as a factory.
 *
 *  Use [[threesixty.visualizer.VisualizationMixins]] to add them to a [[threesixty.visualizer.Visualizer]].
 */
abstract class Visualization(data: Set[ProcessedData]) extends Renderable {

    override def toString(): String = toSVG.toString

}
