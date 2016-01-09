package threesixty.data.tags

import threesixty.data.InputData


trait Tag

/**
 *  Identifies whether data is interpolated data or from the original dataset
 */
trait       InterpolationTag                            extends Tag
object      Interpolated                                extends InterpolationTag
object      Original                                    extends InterpolationTag

/**
 *  Describes the origin of data, either a single set of InputData, multiple
 *  sets or entirely artificially created.
 */
trait       OriginTag                                   extends Tag
case class  InputOrigin(origin:InputData)               extends OriginTag
case class  MultiInputOrigin(origin:Set[InputData])     extends OriginTag
object      ArtificialOrigin                            extends OriginTag
