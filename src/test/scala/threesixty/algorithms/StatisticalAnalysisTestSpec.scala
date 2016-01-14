package threesixty.algorithms.statistics

import org.scalatest._
import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Timestamp
import StatisticalAnalysis._


class DBSCANSpec extends FunSpec {

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

}
