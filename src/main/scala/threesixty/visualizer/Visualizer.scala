package threesixty.visualizer

import threesixty.data.ProcessedData
import threesixty.engine.UsageInfo

import threesixty.visualizer.util.{Grid, Axis}


import spray.json._
import DefaultJsonProtocol._

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


/** Trait for companion objects to  [[threesixty.visualizer.Visualization]]. */
trait VisualizationCompanion extends UsageInfo {
    /** Verbose name of the visualization */
    def name: String

    /** Conversion from String to [[threesixty.visualizer.VisualizationConfig]]. */
    def fromString: (String) => VisualizationConfig
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


object SVGXML {
    implicit def unpimpMulti(pimped: SVGXML): Seq[Elem] = pimped.elems
    implicit def unpimpSingle(pimped: SVGXML): Elem = pimped.elems.head
    implicit def pimpSingle(xml: Elem): SVGXML = SVGXML(xml)
    implicit def pimpMulti(xmls: Seq[Elem]): SVGXML = SVGXML(xmls: _*)
}

case class SVGXML(elems: Elem*) {
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

    def withElem(elem: Elem): SVGXML = SVGXML(elems ++ Seq(elem) :_*)

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

    def withGrid(grid: Grid): SVGXML = SVGXML(Seq[Elem](grid) ++ elems :_*)

    def withAxis(axis: Axis): SVGXML = withElem(axis: Elem)

    // def withLegend(legend: Legend): SVGXML = ???
}
