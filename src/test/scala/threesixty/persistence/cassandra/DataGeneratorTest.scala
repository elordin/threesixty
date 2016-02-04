package threesixty.persistence.cassandra

import java.sql.Timestamp
import java.util.Calendar

import org.scalatest.FunSpec
import threesixty.persistence.ExampleDataGenerator

import scala.util.Random

/**
  * Created by Markus on 30.01.2016.
  */
class DataGeneratorTest extends FunSpec{

  var generator = new ExampleDataGenerator()
  var now = new Timestamp(Calendar.getInstance().getTime().getTime())
  var data = generator.exampleHeartRate(48, 225, 100, new Timestamp(Calendar.getInstance().getTime().getTime()))
  var points = data.dataPoints


  describe("Heart Rate example data") {

    it("heart rate is continious")
    { var diff0 = 0.0
      var diff1 = 0.0
      for(i <- 1 until points.length -1 ){
        diff0 = points(i).value.value - points(i+1).value.value
        diff1 = points(i).value.value - points (i-1).value.value

        assert(math.abs(diff0)<= 5 )
        assert(math.abs(diff1)<= 5 )
      }
    }

    it("heart rate is measured continiuosly every second"){
      var diff = 0.0

      for(i <- 0 until points.length -1 ){
        diff = points(i+1).timestamp.getTime - points(i).timestamp.getTime
       assert(diff == 1000 )
      }
    }


  }


}
