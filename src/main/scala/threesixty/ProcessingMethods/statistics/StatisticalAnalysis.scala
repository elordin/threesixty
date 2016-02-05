package threesixty.ProcessingMethods.statistics

import threesixty.data.Data.Timestamp

import threesixty.data.Implicits._
import threesixty.data.{ProcessedData, TaggedDataPoint, Data}
import Data.ValueType


/**
 *  Collection of various statistical analysis methods
 *
 *  @author Thomas Weber, Jens Woehrle
 */
object StatisticalAnalysis {

    /**
     *  @param data Dataset to calculate the median of
     *  @return Median of input dataset
     */
    def median(data: ProcessedData): ValueType = {
        data.dataPoints.map(_.value).apply(data.dataPoints.length / 2)
    }

    /**
      *  @param data Dataset to calculate the median of
      *  @return Sum of input dataset
      */
    def sum(data: ProcessedData): ValueType = {
        data.dataPoints.map( _.value.value ).sum
    }
    def sum(dataPoints: List[TaggedDataPoint]): ValueType = {
        dataPoints.map( _.value.value ).sum
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @return Mean of input dataset
     */
    def mean(data: ProcessedData): ValueType = {
        data.dataPoints.map(_.value.value).sum / data.dataPoints.length
    }
    def mean(dataPoints: List[TaggedDataPoint]): ValueType = {
        dataPoints.map(_.value.value).sum / dataPoints.length
    }
    def meanTimestamp(dataPoints: List[TaggedDataPoint]): Timestamp = {
        new Timestamp(dataPoints.map(_.timestamp.getTime).sum / dataPoints.length)
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @return Standard deviation of input dataset
     */
    def stdDeviation(data: ProcessedData): ValueType = {
        math.sqrt(variance(data).value)
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @return Variance of input dataset
     */
    def variance(data: ProcessedData): ValueType = {
        val e = mean(data)
        mean(ProcessedData(data.id, data.dataPoints.map { d:TaggedDataPoint =>
            TaggedDataPoint(d.timestamp, math.pow(e.value - d.value.value, 2), d.tags) }))
    }

    // TODO
    def covariance(data1: ProcessedData, data2: ProcessedData): Double = {
        // easy idea with perfect dataset  //
        if( data1.dataPoints.length == data2.dataPoints.length ) {
            val cov = 1.0/(data1.dataPoints.length)
            var sum = 0.0
            for( i <- 0 until data1.dataPoints.length ) {
                sum += (data1.dataPoints(i).value.value - mean(data1).value) * (data2.dataPoints(i).value.value - mean(data2).value)
            }
            cov*sum
        } else {
            // Need to be of equal length or roughly equal time frame //
            val duration1 = timestamp2Long( data1.dataPoints.last.timestamp) - timestamp2Long(data2.dataPoints.head.timestamp)
            val duration2 = timestamp2Long(data2.dataPoints.last.timestamp) - timestamp2Long(data2.dataPoints.head.timestamp)
            // TODO
            //maybe aothomatic interpolation procedure
            0.1
        }
    }

    def correlation(data1: ProcessedData, data2: ProcessedData): Double = {
        covariance(data1, data2)/stdDeviation(data1).value/stdDeviation(data2).value
    }

}
