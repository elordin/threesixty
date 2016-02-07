package threesixty.data

import Data._
import metadata._

import org.scalatest.FunSpec

import threesixty.persistence.FakeDatabaseAdapter

class DataPoolTestSpec extends FunSpec {

    describe("Creating a DataPool without data IDs") {
        it("should throw an IllegalArgumentException") {
            intercept[IllegalArgumentException] {
                new DataPool(Seq(), FakeDatabaseAdapter)
            }
        }
    }

    describe("Creating a DataPool with the FakeDatabaseAdapter") {
        describe("with all default IDs (data1, data2, data3)") {
            val pool = new DataPool(
                Seq(new InputDataSkeleton(
                    "data1", "demodata",
                    CompleteInputMetadata(
                        Timeframe(new Timestamp(23), new Timestamp(104)),
                        Reliability.Unknown,
                        Resolution.Low,
                        Scaling.Ordinal,
                        ActivityType("something"),
                        5000
                    )
                ), new InputDataSkeleton(
                    "data2", "demodata",
                    CompleteInputMetadata(
                        Timeframe(new Timestamp(11), new Timestamp(91)),
                        Reliability.Unknown,
                        Resolution.Low,
                        Scaling.Ordinal,
                        ActivityType("something"),
                        2500
                    )
                ), new InputDataSkeleton(
                    "data3", "demodata",
                    CompleteInputMetadata(
                        Timeframe(new Timestamp(0), new Timestamp(100)),
                        Reliability.Unknown,
                        Resolution.Low,
                        Scaling.Ordinal,
                        ActivityType("something"),
                        400
                    )
                )
            ), FakeDatabaseAdapter)
            it("should have all three datasets available") {
                assert(pool.datasets.size == 3)
                assert(pool.datasets.contains("data1"))
                assert(pool.datasets.contains("data2"))
                assert(pool.datasets.contains("data3"))
            }

            it("should throw a NoSuchElementException when accessing data4") {
                intercept[NoSuchElementException] {
                    pool.getDatasets("data4")
                }
            }

            it("should return the correct dataset when accesing it") {
                var result = pool.getDatasets("data1").toList
                assert(result.length == 1)
                assert(result(0).id == "data1")
                assert(result(0).dataPoints.size == 5000)
            }
        }
    }
}
