package threesixty.persistence

import java.sql.Timestamp
import java.util.{UUID, Calendar}

import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._
import threesixty.data.Data.IntValue

import org.joda.time.DateTime

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


    def gaussianDatapoints(t: DateTime): List[DataPoint] = {
        def gauss(e: Double, v: Double)(t: Long): Int =
            (1 / math.sqrt(2 * math.Pi * math.pow(v, 2)) * math.exp(-1/2 * t - e / math.pow(v, 2))).toInt

        val MIDNIGHT_START = new DateTime(t.getYear, t.getMonthOfYear, t.getDayOfMonth, 0, 0)
        val MIDNIGHT_END = new DateTime(t.getYear, t.getMonthOfYear, t.getDayOfMonth, 23, 59)
        val MORNING_INTENSITY = 100
        val MIDDAY_INTENSITY = 150
        val EVENING_INTENSITY = 100
        val MORNING_TIMESTAMP = (MIDNIGHT_START.getMillis * 3 + MIDNIGHT_END.getMillis) / 4
        val MIDDAY_TIMESTAMP = (MIDNIGHT_END.getMillis + MIDNIGHT_END.getMillis) / 2
        val EVENING_TIMESTAMP = (MIDNIGHT_START.getMillis + MIDNIGHT_END.getMillis * 3) / 4

        val STEP = 10 * 60 * 1000

        (for { i <- 0 to ((MIDNIGHT_END.getMillis - MIDNIGHT_START.getMillis) / STEP).toInt } yield {
            val t: Long = MIDNIGHT_START.getMillis + i * STEP
            DataPoint(new Timestamp(t), IntValue(
                MORNING_INTENSITY   * gauss(MORNING_TIMESTAMP, 4 * 60 * 60 * 1000)(t)
                + MIDDAY_INTENSITY  * gauss(MIDDAY_TIMESTAMP,  4 * 60 * 60 * 1000)(t)
                + EVENING_INTENSITY * gauss(EVENING_TIMESTAMP, 4 * 60 * 60 * 1000)(t)
            ))
        }).toList
    }
}

object Test extends App {


    (new ExampleDataGenerator).gaussianDatapoints(new DateTime()).map(println)

}
