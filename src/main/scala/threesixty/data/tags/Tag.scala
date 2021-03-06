package threesixty.data.tags

import threesixty.data.InputDataLike

trait Tag

/**
 *  Identifies whether data is interpolated data or from the original dataset
 */
trait       InterpolationTag                            extends Tag
object      Interpolated                                extends InterpolationTag {
    override def toString(): String = "interpolated"
}
object      Original                                    extends InterpolationTag {
    override def toString(): String = "original"
}


trait ClusteringTag                                     extends Tag
object NoiseTag                                         extends ClusteringTag {
    override def toString(): String = "noise"
}
case class ClusterTag(id: Int)                          extends ClusteringTag {
    override def toString(): String = "cluster-" + id
}

/**
 *  Describes the origin of data, either a single set of InputData, multiple
 *  sets or entirely artificially created.
 */
trait       OriginTag                                   extends Tag
case class  InputOrigin(origin: InputDataLike)          extends OriginTag {
    override def toString() = s"from-${origin.id}"
}
case class  MultiInputOrigin(origin:Set[InputDataLike]) extends OriginTag {
    override def toString() = origin.foldLeft("") { (s, o) => s + " from-" + o.id }
}
object      ArtificialOrigin                            extends OriginTag {
    override def toString() = "artificial"
}

/**
 *  Describes the name of the aggregation
 *
 *  @param name name of the aggregated data
 */
case class AggregationTag(val name: String)             extends Tag {
    override def toString() = name
}

trait       ChangedDataTag                              extends Tag
object      Accumulated                                 extends ChangedDataTag {
    override def toString(): String = "accumulated"
}

object      TimeAggregated                              extends ChangedDataTag {
    override def toString(): String = "timeaggregated"
}


