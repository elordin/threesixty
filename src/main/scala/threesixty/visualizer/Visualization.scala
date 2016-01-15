package threesixty.visualizer

import scala.xml.{Elem => XMLElem}

trait Visualization {

    override def toString(): String = toSVG.toString

    def toSVG:XMLElem

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}
