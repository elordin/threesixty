package threesixty.persistence

import java.util.UUID

import threesixty.data.{InputData, DataPoint}
import threesixty.data.Data._
import threesixty.data.Implicits._
import threesixty.data.metadata._
import scala.util.Random


object FakeDatabaseAdapter extends DatabaseAdapter {

    def data = database

    def generateDatapointSeries(low: Int, high: Int, start: Long, end: Long, amount: Int): List[DataPoint] = {
        var t: Long = start
        var v: Int = low
        (for (i <- 1 to amount) yield {
            t += (end - start) / amount + Random.nextInt(4) - 2
            v = low + ((high - low) * Random.nextFloat).toInt
            DataPoint(t, IntValue(v))
        }).toList
    }

    def generateDatapointSeries(low: Double, high: Double, start: Long, end: Long, amount: Int): List[DataPoint] = {
        var t: Long = start
        var v: Double = low
        (for (i <- 1 to amount) yield {
            t += (end - start) / amount + Random.nextInt(4) - 2
            v = low + (high - low) * Random.nextFloat
            DataPoint(t, DoubleValue(v))
        }).toList
    }

    var database: Map[Identifier, InputData] = Map(
        "data1" -> InputData(
            "data1", "demodata",
            generateDatapointSeries(60, 120, 23, 104, 26),
            CompleteInputMetadata(
                Timeframe(new Timestamp(23), new Timestamp(104)),
                Reliability.Unknown,
                Resolution.Low,
                Scaling.Ordinal,
                ActivityType("something")
            )
        ),
        "data2" -> InputData(
            "data2", "demodata",
            generateDatapointSeries(71, 94, 11, 91, 10),
            CompleteInputMetadata(
                Timeframe(new Timestamp(11), new Timestamp(91)),
                Reliability.Unknown,
                Resolution.Low,
                Scaling.Ordinal,
                ActivityType("something")
            )
        ),
        "data3" -> InputData(
            "data3", "demodata",
            generateDatapointSeries(3, 41, 0, 100, 14),
            CompleteInputMetadata(
                Timeframe(new Timestamp(0), new Timestamp(100)),
                Reliability.Unknown,
                Resolution.Low,
                Scaling.Ordinal,
                ActivityType("something")
            )
        ),
        "lineTest" -> InputData (
            "lineTest", "demodata",
            List(
                new DataPoint(new Timestamp(10), new DoubleValue(150)),
                new DataPoint(new Timestamp(50), new DoubleValue(375)),
                new DataPoint(new Timestamp(80), new DoubleValue(225)),
                new DataPoint(new Timestamp(85), new DoubleValue(550))),
            CompleteInputMetadata(
                Timeframe(new Timestamp(10), new Timestamp(85)),
                Reliability.Unknown,
                Resolution.Low,
                Scaling.Ordinal,
                ActivityType("something new")
            )
        )
    )


    def getDataset(id:Identifier):Either[String, InputData] =
        database.get(id) match {
            case Some(data) => Right(data)
            case None       => Left(s"No data for id $id found.")
        }


    def insertData(data:InputData):Either[String, Identifier] =
        if (database.contains(data.identifier)) {
            Left(s"Dataset with id ${data.identifier} exists already.")
        } else {
            database += (data.identifier -> data)
            Right(data.identifier)
        }


    def appendData(inData:InputData, id:Identifier):Either[String, Identifier] = {
        database.get(id) match {
            case Some(InputData(_, _, olddata, _)) =>
                database += (id -> inData.copy(dataPoints = olddata ++ inData.dataPoints))
                Right(id)
            case None =>
                Left(s"No data with id $id found")
        }
    }


    def appendOrInsertData(data:InputData, id:Identifier):Either[String, Identifier] =
        appendData(data, id) match {
            case Left(e) => insertData(data)
            case success => success
        }

}
