package threesixty.data.tags

import threesixty.data.InputData


trait Tag


trait  InterpolationTag extends Tag
object Interpolated     extends InterpolationTag
object Original         extends InterpolationTag


case class OriginTag(origin:InputData) extends Tag
case class MultiOriginTag(origin:Set[InputData]) extends Tag
