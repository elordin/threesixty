package threesixty.persistence

import java.sql.Timestamp
import java.util.{UUID, Calendar}

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

        var dataPoints : List[DataPoint] = List()
        val random = new Random()
        var value = 75

        for (i <- 0 until steps){
            var r = random.nextInt(5)
            var delta = r match{
                case 0 => -4
                case 1 => -2
                case 2 => -1
                case 3 => 3
                case 4 => 5
            }

            value += delta
            if(value > max)
            {value = max }
            else if(value < min){
                value = min }

                dataPoints = dataPoints ++ List(new DataPoint(new Timestamp(startTime.getTime + i*1000), value))

            }

            InputData(identifier.toString, measurement, dataPoints, inputMetadta)

        }

    }
