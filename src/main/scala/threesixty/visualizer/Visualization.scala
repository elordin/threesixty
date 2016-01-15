package threesixty.visualizer

import threesixty.data.ProcessedData
import scala.xml.Elem

abstract class Visualization(data: Set[ProcessedData]) {

    def toSVG:Elem

    // def toPNG:PNGImage

    // def toJPG:JPGImage

    // def toRawdata:String
}
