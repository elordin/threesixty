package threesixty.algorithms.statistics

import threesixty.data.{ProcessedData, TaggedDataPoint, Data}
import Data.ValueType

/**
 *  Collection of various statistical analysis methods
 *
 *  @author Thomas Weber
 */
object StatisticalAnalysis {

    /**
     *  @param data Dataset to calculate the median of
     *  @returns Median of input dataset
     */
    def median(data: ProcessedData): ValueType = {
        data.data.map(_.value).apply(data.data.length / 2)
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @returns Mean of input dataset
     */
    def mean(data: ProcessedData): ValueType = {
        data.data.map(_.value.value).sum / data.data.length
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @returns Standard deviation of input dataset
     */
    def stdDeviation(data: ProcessedData): ValueType = {
        math.sqrt(variance(data).value)
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @returns Variance of input dataset
     */
    def variance(data: ProcessedData): ValueType = {
        val e = mean(data)
        mean(ProcessedData(data.id, data.data.map { d:TaggedDataPoint =>
            TaggedDataPoint(d.timestamp, math.pow(e.value - d.value.value, 2), d.tags) }))
    }

    def covariance(data1: ProcessedData, data2: ProcessedData): Double = {
        // easy idea with perfect dataset  //
        if( data1.data.length == data2.data.length ) {
            val cov = 1/(data1.data.length)
            val sum = 0
            for( i <- 0 until data1.data.length ) {
                sum += (data1.data(i) - mean(data1))*(data2.data(i) - mean(data2))
            }
            cov*sum
        } else {
            // Need to be of equal length or roughly equal time frame //
            val duration1 = data1.data.last.timestamp - data2.data.head.timestamp
            val duration2 = data2.data.last.timestamp - data2.data.head.timestamp
        }
    }

    def correlation(data1: ProcessedData, data2: ProcessedData): Double = {
        covariance(data1, data2)/stdDeviation(data1)/stdDeviation(data2)
    }

}
