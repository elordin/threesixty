package threesixty.persistence

import java.sql.Timestamp
import java.util.{UUID, Calendar}
import threesixty.data.Data.Identifier
import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._
import threesixty.data.Data.IntValue

import org.joda.time.DateTime

import scala.util.Random

class ExampleDataGenerator {

    val scaling = Scaling.Ordinal
    val reliability = Reliability.Device
    val resolution = Resolution.High
    val activityType = new ActivityType("Everyday Life")

    implicit def ordered: Ordering[Timestamp] = new Ordering[Timestamp] {
        def compare(x: Timestamp, y: Timestamp): Int = x compareTo y
    }


    def generateStepsForMonth(month: DateTime, withIdentifier: Identifier): InputData = {

        var dataPoints: List[DataPoint] = List()
        for { day <- 1 to month.dayOfMonth.get} {
            val date = new DateTime(month.year.get, month.monthOfYear.get, day, 0, 0)
            dataPoints = dataPoints ++ gaussianDatapoints(date)
        }

        val startDate = dataPoints.minBy(_.timestamp).timestamp
        val endDate = dataPoints.maxBy(_.timestamp).timestamp
        val timeframe = Timeframe(startDate, endDate)
        val metadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, dataPoints.length)
        InputData(withIdentifier, "Step Numbers", dataPoints, metadata)
    }

    def generateStepsForDay(day: DateTime, withIdentifier: Identifier): InputData = {
        var dataPoints = gaussianDatapoints(day)
        val startDate = dataPoints.minBy(_.timestamp).timestamp
        val endDate = dataPoints.maxBy(_.timestamp).timestamp
        val timeframe = Timeframe(startDate, endDate)
        val metadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, dataPoints.length)
        InputData(withIdentifier, "Step Numbers", dataPoints, metadata)
    }

    def gaussianDatapoints(t: DateTime): List[DataPoint] = {
        val variation = 1 + new Random().nextDouble()

        def gauss(mean: Double, stdDev: Double)(t: Long): Double =
            10000000 * (math.exp(math.pow((t - mean)/stdDev, 2) / -2.0 ) / math.sqrt(2.0 * math.Pi) / stdDev)

        val MIDNIGHT_START = new DateTime(t.getYear, t.getMonthOfYear, t.getDayOfMonth, 0, 0)
        val MIDNIGHT_END = new DateTime(t.getYear, t.getMonthOfYear, t.getDayOfMonth, 23, 59)
        val MORNING_INTENSITY = 180 * variation
        val MIDDAY_INTENSITY = 650 * variation
        val EVENING_INTENSITY = 270 * variation
        val MORNING_TIMESTAMP = (MIDNIGHT_START.getMillis * 5 + MIDNIGHT_END.getMillis * 2) / 7
        val MIDDAY_TIMESTAMP = (MIDNIGHT_START.getMillis * 4 + MIDNIGHT_END.getMillis * 5) / 9
        val EVENING_TIMESTAMP = (MIDNIGHT_START.getMillis * 1 + MIDNIGHT_END.getMillis * 6) / 7

        val STEP = 10 * 60 * 1000

        (for { i <- 0 to ((MIDNIGHT_END.getMillis - MIDNIGHT_START.getMillis) / STEP).toInt } yield {
            val t: Long = MIDNIGHT_START.getMillis + i * STEP
            DataPoint(new Timestamp(t), IntValue((
                (MORNING_INTENSITY   * gauss(MORNING_TIMESTAMP, 1.2  * 60 * 60 * 1000)(t)
                    + MIDDAY_INTENSITY  * gauss(MIDDAY_TIMESTAMP,  3 * 60 * 60 * 1000)(t)
                    + EVENING_INTENSITY * gauss(EVENING_TIMESTAMP, 1.6 * 60 * 60 * 1000)(t)) / 3
                    + (if (t < (MIDDAY_TIMESTAMP + MIDNIGHT_START.getMillis) / 2) 0 else Random.nextInt(10))
                ).toInt))
        }).toList
    }




    def     exampleHeartRate(min: Int = 48, max: Int = 225, steps: Int,
                         startTime: Timestamp = new Timestamp(Calendar.getInstance().getTime().getTime())): InputData = {

        val identifier = UUID.randomUUID()
        val measurement = "Heart Rate"
        val timeframe = Timeframe(startTime, new Timestamp(startTime.getTime() + steps.toLong * 1000L))
        val activityType = ActivityType("nothing special")
        activityType.setDescription("everyday tracking heartrate")
        val resolution = Resolution.High
        val reliability = Reliability.Device
        val scaling = Scaling.Ordinal
        val size = steps

        val inputMetadta = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, size)

        var dataPoints: List[DataPoint] = List()
        val random = new Random()
        var value = 75

        for (i <- 0 until steps) {

            var r = random.nextInt(5)
            var delta = r match {
                case 0 => -4
                case 1 => -2
                case 2 => -1
                case 3 => 2
                case 4 => 5
            }

            value += delta
            if (value > max) {
                value = max
            }
            else if (value < min) {
                value = min
            }

            println("Step: " + i + " Value: " + value)

            dataPoints = dataPoints ++ List(new DataPoint(new Timestamp(startTime.getTime + i * 1000), value))
        }
        InputData(identifier.toString, measurement, dataPoints, inputMetadta)
    }

}
