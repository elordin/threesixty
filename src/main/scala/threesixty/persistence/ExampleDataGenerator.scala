package threesixty.persistence

import java.sql.Timestamp
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


    /*
    def generateStepsForDate(date: DateTime): List[DataPoint] = {
        var stepDataPoints: List[DataPoint] = List()

        val year = date.year.get
        val month = date.monthOfYear.get
        val day = date.dayOfMonth.get

        val frequency = 10
        for {hour <- 1 until 24} {
            for {minute <- 0 until (60 / frequency)} {
                val datetime = new DateTime(year, month, day, hour, minute * frequency)

                val timestamp = new Timestamp(datetime.getMillis)

                val stepValue = getRandomStepValueForHour(hour, frequency)

                val dataPoint = new DataPoint(timestamp, stepValue)
                stepDataPoints = dataPoint :: stepDataPoints
            }
        }
        stepDataPoints
    }

    def getRandomStepValueForHour(hour: Int, frequency: Int): Int = {
        var stepValue = 0
        if (hour > 7 && hour < 23) {
            if ((hour >= 9 && hour < 10) || (hour >= 12 && hour < 14) || (hour >= 17 && hour < 19)) {
                stepValue = new Random().nextInt(frequency * 30)
            } else {
                stepValue = new Random().nextInt(frequency * 10)
            }
        }
        stepValue
    }
    */




    /*
    def exampleHeartRate(
                            min: Int = 48,
                            max: Int = 225,
                            steps: Int,
                            startTime: Timestamp = new Timestamp(Calendar.getInstance().getTime().getTime())
                        ): InputData = {

        val identifier = UUID.randomUUID()
        val measurement = "Heart Rate"
        val timeframe = Timeframe(startTime, new Timestamp(startTime.getTime() + steps * 1000))
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
                case 3 => 3
                case 4 => 5
            }

            value += delta
            if (value > max) {
                value = max
            }
            else if (value < min) {
                value = min
            }
            dataPoints = dataPoints ++ List(new DataPoint(new Timestamp(startTime.getTime + i * 1000), value))
        }
        InputData(identifier.toString, measurement, dataPoints, inputMetadta)
    }
    */



}
