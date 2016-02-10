package threesixty.data


import threesixty.data.tags._
import threesixty.data.Data._
import threesixty.data.metadata.{CompleteInputMetadata, Timeframe, Reliability, Resolution, Scaling, ActivityType}

import org.scalatest._


class DataTestSpec extends FunSpec {

    describe("InputData") {
        describe("when created without data") {
            describe("should throw an IllegalArgumentException") {
                it("when the data is empty") {
                    intercept[IllegalArgumentException] {
                        val data = InputData("", "", Nil, CompleteInputMetadata(
                                Timeframe(new Timestamp(0), new Timestamp(1)),
                                Reliability.Unknown,
                                Resolution.Low,
                                Scaling.Ordinal,
                                ActivityType("something"),
                                1
                            )
                        )
                    }
                }
                it("when metadata specifies an empty dataset") {
                    intercept[IllegalArgumentException] {
                        val data = InputData("", "", List(DataPoint(new Timestamp(0), DoubleValue(5.0))), CompleteInputMetadata(
                                Timeframe(new Timestamp(0), new Timestamp(1)),
                                Reliability.Unknown,
                                Resolution.Low,
                                Scaling.Ordinal,
                                ActivityType("something"),
                                0
                            )
                        )
                    }
                }
                it("when length of datapoints differs from the metadata field") {
                    intercept[IllegalArgumentException] {
                        val data = InputData("", "", List(DataPoint(new Timestamp(0), DoubleValue(5.0))), CompleteInputMetadata(
                                Timeframe(new Timestamp(0), new Timestamp(1)),
                                Reliability.Unknown,
                                Resolution.Low,
                                Scaling.Ordinal,
                                ActivityType("something"),
                                5
                            )
                        )
                    }
                }
            }
        }
    }

    describe("ProcessedData") {
        describe("when created without data") {
            it("should throw an IllegalArgumentException") {
                intercept[IllegalArgumentException] {
                    val data = ProcessedData("", Nil)
                }
            }
        }

        val data1 = ProcessedData("data1", List(
                TaggedDataPoint(new Timestamp(10), 1.0, Set()),
                TaggedDataPoint(new Timestamp(20), 2.0, Set())
            ))
        describe("when joined with another dataset") {


            val data2 = ProcessedData("data2", List(
                    TaggedDataPoint(new Timestamp(40), 4.0, Set()),
                    TaggedDataPoint(new Timestamp(20), 5.0, Set())
                ))
            describe("using a cross join") {
                it("should have all combinations") {
                    assertResult(data1.join(data2)) {
                        List(
                            (TaggedDataPoint(new Timestamp(10), 1.0, Set()), TaggedDataPoint(new Timestamp(40), 4.0, Set())),
                            (TaggedDataPoint(new Timestamp(10), 1.0, Set()), TaggedDataPoint(new Timestamp(20), 5.0, Set())),
                            (TaggedDataPoint(new Timestamp(20), 2.0, Set()), TaggedDataPoint(new Timestamp(40), 4.0, Set())),
                            (TaggedDataPoint(new Timestamp(20), 2.0, Set()), TaggedDataPoint(new Timestamp(20), 5.0, Set()))
                        )
                    }
                }
            }

            describe("using an equi join") {
                it("should have the tuples that fulfill the predicate") {
                    assertResult(data1.equiJoin(data2, _.timestamp.getTime)) {
                        List(
                            (TaggedDataPoint(new Timestamp(20), 2.0, Set()), TaggedDataPoint(new Timestamp(20), 5.0, Set()))
                        )
                    }
                }
            }
        }
    }

    describe("The implicit conversion") {
        describe("of InputData with data (0,0), (5,5) to ProcessedData") {
            val inputData:InputData = InputData("", "", List(
                DataPoint(new Timestamp(0), 0.0),
                DataPoint(new Timestamp(5), 5.0)
            ), CompleteInputMetadata(
                    Timeframe(new Timestamp(0), new Timestamp(1)),
                    Reliability.Unknown,
                    Resolution.Low,
                    Scaling.Ordinal,
                    ActivityType("something"),
                    2
                )
            )

            val processedData:ProcessedData = inputData

            it("should have the same data and InputOrigin Tags") {


                assertResult(processedData) {
                    ProcessedData("", List(
                        TaggedDataPoint(new Timestamp(0), 0.0, Set(InputOrigin(inputData))),
                        TaggedDataPoint(new Timestamp(5), 5.0, Set(InputOrigin(inputData)))
                    ))
                }
            }
        }

        describe("between a Double and a DoubleValue") {
            val d: Double = 13.37d
            val dv: DoubleValue = DoubleValue(13.37d)

            it("should turn a Double into a DoubleValue") {
                assert(double2DoubleValue(d) == dv)
            }
            it("should turn a DoubleValue into a Double") {
                assert(d == doubleValue2Double(dv))
            }
            it("should be invertable") {
                val d2dv: DoubleValue = double2DoubleValue(d)
                val dv2d: Double = doubleValue2Double(dv)
                assert(double2DoubleValue(dv2d) == dv)
                assert(doubleValue2Double(d2dv) == d)
            }
        }

        describe("between Int, Double and IntValue") {
            val i: Int = 1337
            val d: Double = 1337d
            val iv: IntValue = IntValue(1337)

            it("should turn an Int into an IntValue") {
                assert(int2IntValue(i) == iv)
            }
            it("should turn an IntValue into a Double") {
                assert(d == intValue2Double(iv))
            }
        }
    }

}
