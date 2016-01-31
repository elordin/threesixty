package threesixty.visualizer

import threesixty.visualizer.util.{Grid, Axis}
import threesixty.engine.UsageInfo

import spray.json._
import DefaultJsonProtocol._
import threesixty.processor.{ProcessingMethod, ProcessingMethodCompanion}

import scala.xml.Elem

trait Renderable {
    /**
     *  Returns a SVG/XML tree.
     */
    def toSVG: Elem

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}

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
            json.fields.getOrElse("type", {
                throw new IllegalArgumentException("parameter \"type\" missing for visualization")
            }).convertTo[String]
        } catch {
            case e: DeserializationException =>
                throw new IllegalArgumentException("Invalid value for parameter \"type\". Should be String.")
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

object SVGXML {
    implicit def unpimpMulti(pimped: SVGXML): Seq[Elem] = pimped.elems
    implicit def unpimpSingle(pimped: SVGXML): Elem = pimped.elems.head
    implicit def pimpSingle(xml: Elem): SVGXML = SVGXML(xml)
    implicit def pimpMulti(xmls: Seq[Elem]): SVGXML = SVGXML(xmls: _*)
}

/**
 *  Pimped Version of scala.xml.Elems that allows pluggin additional
 *  SVG components.
 */
case class SVGXML(elems: Elem*) {
    /** Wraps everything in the SVG tag */
    def withSVGHeader(
            viewBoxX: Int,
            viewBoxY: Int,
            viewBoxWidth: Int,
            viewBoxHeight: Int): SVGXML =
        SVGXML(<svg
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                viewBox={ s"$viewBoxX $viewBoxY $viewBoxWidth $viewBoxHeight" }
                xml:space="preserve">
                { elems }
            </svg>)

    /** Appends an arbitrary element */
    def withElem(elem: Elem): SVGXML = SVGXML(elems ++ Seq(elem) :_*)

    /** Appends a title */
    def withTitle(text: String, x: Int, y: Int, fontSize: Int): SVGXML =
        if (text != "") {
            withElem(<text  x={ x.toString }
                        y={ y.toString }
                        font-family="Roboto, Segoe UI, Sans-Serif"
                        font-weight="100"
                        font-size={ fontSize.toString }
                        text-anchor="middle">{ text }
                </text>)
        } else {
            this
        }

    /** Prepends a grid */
    def withGrid(grid: Grid): SVGXML = SVGXML(Seq[Elem](grid) ++ elems :_*)

    /** Appends an axis */
    def withAxis(axis: Axis): SVGXML = withElem(axis: Elem)

    // def withLegend(legend: Legend): SVGXML = ???
}
