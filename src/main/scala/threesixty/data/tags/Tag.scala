package threesixty.data.tags

import threesixty.data.InputData


trait Tag

/**
 *  Identifies whether data is interpolated data or from the original dataset
 */
trait       InterpolationTag                        extends Tag
object      Interpolated                            extends InterpolationTag {
    override def toString(): String = "interpolated"
}
object      Original                                extends InterpolationTag {
    override def toString(): String = "original"
}

/**
 *  Describes the origin of data, either a single set of InputData, multiple
 *  sets or entirely artificially created.
 */
trait       OriginTag                               extends Tag
case class  InputOrigin(origin:InputData)           extends OriginTag {
    override def toString() = s"from-${origin.id}"
}
case class  MultiInputOrigin(origin:Set[InputData]) extends OriginTag {
    override def toString() = origin.foldLeft("") { (s, o) => s + " from-" + o.id }
}
object      ArtificialOrigin                        extends OriginTag {
    override def toString() = "artificial"
}
