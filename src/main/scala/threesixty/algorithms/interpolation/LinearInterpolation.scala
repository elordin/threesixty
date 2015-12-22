package threesixty.algorithms.interpolation

import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.tags.{Tag, Interpolated, Original}
import threesixty.processor.SingleProcessingMethod

/**
 *  Linear interpolator
 *
 *  @author Thomas Weber
 *  @param resolution Desired max. time-distance between datapoints.
 */
case class LinearInterpolation(resolution:Int) extends SingleProcessingMethod {

    /**
     *  Inserts interpolated values into the dataset and adds tags
     *  to identify interpolated and original values.
     *
     *  @param data Data to interpolate
     */
    def apply(data: ProcessedData):ProcessedData = {

        /**
         *  Interpolation function.
         *  For each combination of two points it creates the linear
         *  equation paramters m (slope) and b (offset).
         *  It the generates the appropriate number of intermediary points
         *  with the corresponding values and tags and inserts them into
         *  the list of datapoints.
         *
         *  @param list of datapoints
         *  @returns list of datapoints with interpolated values and Tnterpolation-tags
         */
        def linearInterpolated: List[TaggedDataPoint] => List[TaggedDataPoint] = {
            case d1@TaggedDataPoint(t1, v1, tags1) :: (d2@TaggedDataPoint(t2, v2, tags2) :: ds) =>
                if (t2 - t1 > resolution) {

                    val m = ((v2 - v1) / (t2 - t1))
                    val b = v1 - m * t1

                    def interpolFunc(x:Int):TaggedDataPoint = TaggedDataPoint(x, m * x + b, Set[Tag](Interpolated))

                    TaggedDataPoint(t1, v1, tags1 + Original) :: Range(t1 + resolution, t2, resolution).map(interpolFunc).toList ++ linearInterpolated( TaggedDataPoint(t2, v2, tags2 + Original) :: ds )

                } else {
                    TaggedDataPoint(t1, v1, tags1 + Original) :: linearInterpolated( TaggedDataPoint(t2, v2, tags2 + Original) :: ds )
                }
            case otherwise => otherwise
        }

        val orderedDataPoints = data.data.sortBy(_.timestamp)

        data.copy(data = linearInterpolated(orderedDataPoints))
    }

}
