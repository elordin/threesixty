package threesixty.persistence

import java.sql.Timestamp
import java.util.{UUID, Calendar}

import org.joda.time.DateTime
import threesixty.data.Data.Identifier
import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._

import scala.util.Random

/**
 * Created by Markus on 30.01.2016.
 */
class ExampleDataGenerator {

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





    def generateStepsForCurrentMonthWithIdentifier(identifier: Identifier): InputData = {
        val today = DateTime.now()
        val month = today.monthOfYear()

        val dataPoints = generateStepsForDateWithIdentifier(identifier, today)

        val startTime = dataPoints.minBy(_.timestamp.getTime).timestamp
        val endTime = dataPoints.maxBy(_.timestamp.getTime).timestamp
        val timeframe = Timeframe(startTime, new Timestamp(startTime.getTime()))

        val activityType = new ActivityType("Everyday Life")

        val size = dataPoints.length
        val resolution = Resolution.Middle
        val reliability = Reliability.Device
        val scaling = Scaling.Ordinal
        val metadata = new CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, size)

        new InputData(identifier, "Step Numbers", dataPoints, metadata)
    }

    def generateStepsForDateWithIdentifier(identifier: Identifier, date: DateTime): List[DataPoint] = {
        var stepDataPoints: List[DataPoint] = List()

        val year = date.year.get
        val month = date.monthOfYear.get
        val day = date.dayOfMonth.get

        val frequency = 10
        var sum = 0

        for {hour <- 1 until 24} {
            for {minute <- 0 until (60 / frequency)} {
                val datetime = new DateTime(year, month, day, hour, minute * frequency)

                println(datetime.minuteOfHour().get())

                val timestamp = new Timestamp(datetime.getMillis)
                val stepValue = getRandomStepValueForHour(hour, frequency)

                val dataPoint = new DataPoint(timestamp, stepValue)
                stepDataPoints = dataPoint :: stepDataPoints

                sum += stepValue
                // println("Hour " + hour + ", Minute: " + minute * frequency + ", Steps: " + stepValue)
            }
        }
        // println("Sum: " + sum)

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



}
