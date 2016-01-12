package threesixty.visualizer

import scala.xml.{Elem => XMLElem}

trait Visualization {

    def toSVG:XMLElem

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}
