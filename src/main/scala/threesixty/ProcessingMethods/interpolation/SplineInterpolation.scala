package threesixty.ProcessingMethods.interpolation

import threesixty.data.metadata.{Resolution, Scaling}
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint, InputDataSkeleton}
import threesixty.data.Data.{Identifier, Timestamp}
import threesixty.data.Implicits.timestamp2Long
import threesixty.data.tags.{Tag, Interpolated, Original}
import threesixty.processor.{ProcessingMixins, SingleProcessingMethod, ProcessingMethodCompanion, ProcessingStep}

import spray.json._
import DefaultJsonProtocol._
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
// import threesixty.visualizer.visualizations.heatLineChart.HeatLineChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
// import threesixty.visualizer.visualizations.polarAreaChart.PolarAreaChartConfig
// import threesixty.visualizer.visualizations.progressChart.ProgressChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig
// import threesixty.visualizer.visualizations.scatterColorChart.ScatterColorChartConfig

object SplineInterpolation extends ProcessingMethodCompanion {

    trait Mixin extends ProcessingMixins {
        abstract override def processingInfos: Map[String, ProcessingMethodCompanion] =
            super.processingInfos + ("splineinterpolation" -> SplineInterpolation)
    }

    def name = "Spline Interpolation"

    def fromString: (String) => ProcessingStep = { s => apply(s).asProcessingStep }

    def usage = """ The spline interpolation is currently out of order """

    def apply(jsonString: String): SplineInterpolation = {
        implicit val splineInterpolationFormat =
            jsonFormat({ idm: Map[Identifier, Identifier] => SplineInterpolation.apply(idm) }, "idMapping")
        jsonString.parseJson.convertTo[SplineInterpolation]
    }

    def default(idMapping: Map[Identifier, Identifier]): ProcessingStep =
        SplineInterpolation(idMapping).asProcessingStep

    def computeDegreeOfFit(inputData: InputDataSkeleton): Double = {

        var temp = 0.0
        val meta = inputData.metadata

        if (meta.scaling == Scaling.Ordinal) {
            temp += 0.4
        }
        if (meta.size >= 5) {
            temp += 1.0
        }
        if (meta.size >= 50) {
            temp += 0.6 //overall 0.4 because >= 50 includes >= 5
        }
        if (meta.resolution == Resolution.High) {
            temp += 0.0
        }
        if (meta.resolution == Resolution.Middle) {
            temp += 1.0
        }

        temp
    }

    def computeDegreeOfFit(targetVisualization: VisualizationConfig, inputData: InputDataSkeleton): Double = {

        val strategyFactor = computeDegreeOfFit(inputData)
        val visFactor = targetVisualization match {
            //good
            case _:LineChartConfig          => 1.0
            // case _:HeatLineChartConfig      => 1.0
            case _:BarChartConfig           => 0.8
            // case _:PolarAreaChartConfig     => 0.8 //equal to BarChar
            //bad
            case _:ScatterChartConfig       => 0.2
            // case _:ScatterColorChartConfig  => 0.2
            // case _:ProgressChartConfig      => 0.1
            case _:PieChartConfig           => 0.1
            //default
            case _                          => 0.5
        }

        strategyFactor * visFactor
    }

}


/**
  *  Spline interpolator
  *  Creates the spline interpolated function out of a set of data
  *
  *  @author Jens Woehrle
  */
case class SplineInterpolation(idMapping: Map[Identifier, Identifier])
    extends SingleProcessingMethod {

    def companion: ProcessingMethodCompanion = SplineInterpolation

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
          *  Spline interplato function.
          *  For each combination of two points it creates the cubic
          *  equation paramters dx^3+cx^2+bx+y
          *  and returns a list with its Qudruple
          *
          *  @param list of datapoints
          *  @returns list of qudruples of the spline interpolation
          */
        val odata = data.dataPoints.sortBy(d => timestamp2Long(d.timestamp))
        val x = new Array[Long](odata.length)
        val y = new Array[Double](odata.length)
        for( j <- 0 until odata.length) {
            x(j) = timestamp2Long(odata(j).timestamp)
            y(j) = odata(j).value.value
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
        var z: Array[Double] = Array.fill(n+1)(0)
        var i = 1
        while (i < n) {
            val g = 2.0 * (x(i+1) - x(i-1)) - h(i-1) * mu(i-1)
            mu(i) = h(i) / g
            z(i) = (3.0 * (y(i+1) * h(i-1) - y(i) * (x(i+1) - x(i-1))+ y(i-1) * h(i)) /
             (h(i-1) * h(i)) - h(i-1) * z(i-1)) / g
            i += 1
        }

        // cubic spline coefficients --  b is linear, c quadratic, d is cubic (original y's are constants)
        var b: Array[Double] = Array.fill(n)(0)
        var c: Array[Double] = Array.fill(n+1)(0)
        var d: Array[Double] = Array.fill(n)(0)

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
        var tp = 0;

        //val dataPoints :  List[TaggedDataPoint] = new TaggedDataPoint(new Timestamp(x(0)), y(0), data.data(0).tags)
        var dataPoints = List[TaggedDataPoint]()
        while( tp < n ) {
            //dataPoints += new TaggedDataPoint(new Timestamp(x(0)), y(0), data.data(0).tags)
            tp += 1
            //einfügen dern andere...
        }

        val newID = idMapping(data.id)

        Set(new ProcessedData(newID, dataPoints))
    }
}
