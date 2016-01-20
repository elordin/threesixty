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
        data.dataPoints.map(_.value).apply(data.dataPoints.length / 2)
    }

    /**
     *  @param data Dataset to calculate the median of
     *  @returns Mean of input dataset
     */
    def mean(data: ProcessedData): ValueType = {
        data.dataPoints.map(_.value.value).sum / data.dataPoints.length
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
        mean(ProcessedData(data.id, data.dataPoints.map { d:TaggedDataPoint =>
            TaggedDataPoint(d.timestamp, math.pow(e.value - d.value.value, 2), d.tags) }))
    }

    // TODO
    def covariance(data1: ProcessedData, data2: ProcessedData): Double = {
        throw new NotImplementedError
    }

    // TODO
    def correlation(data1: ProcessedData, data2: ProcessedData): Double = {
        throw new NotImplementedError
    }

}
