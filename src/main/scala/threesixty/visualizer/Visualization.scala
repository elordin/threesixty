package threesixty.visualizer

import scala.xml.{Elem => XMLElem}

trait Visualization {

    val width:Int

    val height:Int

    def toSVG:XMLElem

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}
