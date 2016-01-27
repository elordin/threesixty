package threesixty.algorithms.statistics

import org.scalatest._
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Timestamp
import StatisticalAnalysis._


class DBSCANSpec extends FunSpec {

    val e = 0.001
    describe("The dataset [0.1, 2.4, 5.1, 7.2, 9.2, 12.0]") {
        val sampleData = ProcessedData("", List(
            TaggedDataPoint(new Timestamp(0),  0.1, Set()),
            TaggedDataPoint(new Timestamp(0),  2.4, Set()),
            TaggedDataPoint(new Timestamp(0),  5.1, Set()),
            TaggedDataPoint(new Timestamp(0),  7.2, Set()),
            TaggedDataPoint(new Timestamp(0),  9.2, Set()),
            TaggedDataPoint(new Timestamp(0), 12.0, Set())
        ))

        it("should have a median of 5.1") {
            assert( median(sampleData).value == 7.2 )
        }

        it("should have a mean of 6.0") {
            assert( mean(sampleData).value == 6.0 )
        }

        it("should have a variance of about 16.043") {
            assert( math.round(variance(sampleData).value * 1000) ==  16043 )
        }

        it("should have a standard deviation of about 4.005") {
            assert( math.round(stdDeviation(sampleData).value * 1000) == 4005)
        }
    }

    //Easy correlation and covariance claculation
    describe("The dataset [0.1, 2.4, 5.1, 7.2, 9.2, 12.0] && [0.2, 2.5, 5.3, 6.2, 11.1, 12.0]") {
        val sampleData1 = ProcessedData("", List(
            TaggedDataPoint(new Timestamp(0),  0.1, Set()),
            TaggedDataPoint(new Timestamp(1),  2.4, Set()),
            TaggedDataPoint(new Timestamp(2),  5.1, Set()),
            TaggedDataPoint(new Timestamp(3),  7.2, Set()),
            TaggedDataPoint(new Timestamp(4),  9.2, Set()),
            TaggedDataPoint(new Timestamp(5), 12.0, Set())
        ))

        val sampleData2 = ProcessedData("", List(
            TaggedDataPoint(new Timestamp(0),  0.2, Set()),
            TaggedDataPoint(new Timestamp(1),  2.5, Set()),
            TaggedDataPoint(new Timestamp(2),  5.3, Set()),
            TaggedDataPoint(new Timestamp(3),  6.2, Set()),
            TaggedDataPoint(new Timestamp(4), 11.1, Set()),
            TaggedDataPoint(new Timestamp(5), 12.0, Set())
        ))

        // TODO Values
        it("should have a covariance of 5.1") {
            val cov = covariance(sampleData1, sampleData2)
            assert( 16.668 - e <= cov )
            assert( cov <= 16.668 + e )
        }

        it("should have a correlation of 6.0") {
            val cor = correlation(sampleData1, sampleData2)
            assert( cor <= 0.980 + e )
            assert( 0.980 - e <= cor )
        }

    }

}
