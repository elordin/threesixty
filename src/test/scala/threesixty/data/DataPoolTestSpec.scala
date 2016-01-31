package threesixty.data

import org.scalatest.FunSpec

import threesixty.persistence.FakeDatabaseAdapter

class DataPoolTestSpec extends FunSpec {

    describe("Creating a DataPool without data IDs") {
        it("should throw an IllegalArgumentException") {
            intercept[IllegalArgumentException] {
                new DataPool(Set(), FakeDatabaseAdapter)
            }
        }
    }

    describe("Creating a DataPool with the FakeDatabaseAdapter") {
        describe("with all default IDs (data1, data2, data3)") {
            val pool = new DataPool(
                Set("data1", "data2", "data3"),
                FakeDatabaseAdapter)
            it("should have all three datasets available") {
                assert(pool.datasets.size == 3)
                assert(pool.datasets.contains("data1"))
                assert(pool.datasets.contains("data2"))
                assert(pool.datasets.contains("data3"))
            }

            it("should throw a NoSuchElementException when accessing data4") {
                intercept[NoSuchElementException] {
                    pool.getDatasets(Set("data4"))
                }
            }

            it("should return the correct dataset when accesing it") {
                var result = pool.getDatasets(Set("data1")).toList
                assert(result.length == 1)
                assert(result(0).id == "data1")
                assert(result(0).dataPoints.size == 26)
            }
        }
    }
}
