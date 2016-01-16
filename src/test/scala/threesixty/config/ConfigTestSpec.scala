package threesixty.config

import org.scalatest.FunSpec

import threesixty.persistence.FakeDatabaseAdapter

class ConfigTestSpec extends FunSpec {

    describe("Creating a Config without data IDs") {
        it("should throw an IllegalArgumentException") {
            intercept[IllegalArgumentException] {
                new Config(Set(), FakeDatabaseAdapter)
            }
        }
    }

    describe("Creating a Config with the FakeDatabaseAdapter") {
        describe("with all default IDs (data1, data2, data3)") {
            val config = new Config(
                Set("data1", "data2", "data3"),
                FakeDatabaseAdapter)
            it("should have all three datasets available") {
                assert(config.datasets.size == 3)
                assert(config.datasets.contains("data1"))
                assert(config.datasets.contains("data2"))
                assert(config.datasets.contains("data3"))
            }

            it("should throw a NoSuchElementException when accessing data4") {
                intercept[NoSuchElementException] {
                    config.getDatasets(Set("data4"))
                }
            }

            it("should return the correct dataset when accesing it") {
                var result = config.getDatasets(Set("data1")).toList
                assert(result.length == 1)
                assert(result(0).id == "data1")
                assert(result(0).data.size == 26)
            }
        }
    }
}
