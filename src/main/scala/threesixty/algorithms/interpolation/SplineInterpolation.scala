package threesixty.algorithms.interpolation

import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.{ValueType, Identifier, Timestamp}
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.tags.{Tag, Interpolated, Original}
import threesixty.processor.SingleProcessingMethod

/**
  *  Linear interpolator
  *
  *  @author Jens Woehrle
  *  @param resolution Desired max. time-distance between datapoints.
  */
case class SplineInterpolation(resolution:Int, idMapping: Map[Identifier, Identifier])
  extends SingleProcessingMethod(idMapping: Map[Identifier, Identifier]) {

  /**
    *  Created a new dataset with ID as specified in idMapping.
    *  Inserts interpolated values along the original ones into
    *  this new dataset and adds tags to identify interpolated
    *  and original values.
    *
    *  @param data Data to interpolate
    *  @return One element Set containing the new dataset
    */
  @throws[NoSuchElementException]("if data.id can not be found in idMapping")
  def apply(data: ProcessedData): Set[ProcessedData] = {
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
    val odata = data.data.sortBy(d => timestamp2Long(d.timestamp))
    val x = new Array[Long](odata.length)
    val y = new Array[ValueType](odata.length)
    for( j <- 0 until odata.length) {
      x(j) = timestamp2Long(odata(j).timestamp)
      y(j) = odata(j).value
    }

    if (x.length < 3) {
      // throw new NumberIsTooSmallException(x.length, 3, true)
      throw new NotImplementedError
    }
    // Number of intervals.  The number of data points is n + 1.
    val n = x.length - 1

    // Differences between knot points
    val h = Array.tabulate(n)(i => x(i+1) - x(i))

    var mu: Array[Double] = Array.fill(n)(0)
    var z: Array[ValueType] = Array.fill(n+1)(0)
    var i = 1
    while (i < n) {
      val g = 2.0 * (x(i+1) - x(i-1)) - h(i-1) * mu(i-1)
      mu(i) = h(i) / g
      z(i) = (3.0 * (y(i+1) * h(i-1) - y(i) * (x(i+1) - x(i-1))+ y(i-1) * h(i)) /
        (h(i-1) * h(i)) - h(i-1) * z(i-1)) / g
      i += 1
    }

    // cubic spline coefficients --  b is linear, c quadratic, d is cubic (original y's are constants)
    var b: Array[ValueType] = Array.fill(n)(0)
    var c: Array[ValueType] = Array.fill(n+1)(0)
    var d: Array[ValueType] = Array.fill(n)(0)

    var j = n-1
    while (j >= 0) {
      c(j) = z(j) - mu(j) * c(j + 1)
      b(j) = (y(j+1) - y(j)) / h(j) - h(j) * (c(j+1) + 2.0 * c(j)) / 3.0
      d(j) = (c(j+1) - c(j)) / (3.0 * h(j))
      j -= 1
    }
    // by this point we created the polynomials coefficients of the splines
    // Now we start generating the Datasetpoints
    
    //Array.tabulate(n)(i => Polynomial(Array(y(i), b(i), c(i), d(i))))
  }
}
