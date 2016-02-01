package threesixty.visualizer

import threesixty.data.{InputData, ProcessedData}
import threesixty.engine.UsageInfo

import spray.json._
import DefaultJsonProtocol._
import threesixty.processor.{ProcessingMethod, ProcessingMethodCompanion}

/*
/** Trait for companion objects to  [[threesixty.visualizer.Visualization]]. */
trait VisualizationCompanion extends UsageInfo {
    /** Verbose name of the visualization */
    def name: String

    /** Conversion from String to [[threesixty.visualizer.VisualizationConfig]]. */
    def fromString: (String) => VisualizationConfig
}
*/


/**
 *  Mixin trait for layering [[threesixty.visualizer.Visualization]]s onto the [[threesixty.visualizer.Visualizer]]
 *
 *  Extend this by abstract overriding the visualizationInfos value with super calls.
 *  @example {{{
 *      object FooVisualizationConfig extends VisualizationCompanion {
 *          def name = "Foo"
 *          def fromString(in: String) = ...
 *          def usage = "Use responsibly!"
 *      }
 *
 *      trait FooVisualizationMixin extends VisualizationMixins {
 *          abstract override def visualizationInfos =
 *              super.visualizationInfos + ("foo" -> FooVisualizationConfig)
 *      }
 *
 *      val visualizer = new Visualizer with FooVisualizationMixin
 *  }}}
 */
trait VisualizationMixins {
    /**
     *  Map containing all mixedin Visualizations.
     *  Use an abstract override to extends this.
     */
    def visualizationInfos: Map[String, VisualizationCompanion] = Map.empty
}


/**
 *  Holds a list of available visualizations and some meta information,
 *  including how to convert to [[threesixty.visualizer.VisualizationConfig]].
 *
 *  Stack traits inheriting from [[threesixty.visualizer.VisualizationMixins]] to add visualizations.
 *
 *  @author Thomas Weber
 *
 *  @example {{{
 *      val visualizer = new Visualizer with FooVisualization.Mixin with BarVisualization.Mixin
 *  }}}
 */
class Visualizer extends VisualizationMixins with UsageInfo {
    // TODO Exception catching and proper access

    def usage = " ... "


    @throws[IllegalArgumentException]("if a parameter is missing")
    @throws[NoSuchElementException]("if the json specifies a type that has no conversion")
    def toVisualizationConfig(jsonString: String): VisualizationConfig = {
        val json: JsObject = jsonString.parseJson.asJsObject

        val vizType = try {
            json.getFields("type")(0).convertTo[String]
        } catch {
            case e:IndexOutOfBoundsException =>
                throw new IllegalArgumentException("parameter \"type\" missing for visualization")
        }

        val conversion: (String) => VisualizationConfig =
            this.visualizationInfos.getOrElse(vizType,
                throw new NoSuchElementException(s"Unknown visualization type $vizType")
            ).fromString

        val args: String = json.fields.get("args") match {
            case Some(jsonVal) => jsonVal.toString // get args from visualization
            case None          => throw new IllegalArgumentException("parameter \"args\" missing for visualization")
        }

        conversion(args)
    }

}
