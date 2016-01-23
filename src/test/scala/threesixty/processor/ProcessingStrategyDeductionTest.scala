package threesixty.processor

import org.scalatest.FunSpec
import threesixty.algorithms.interpolation.LinearInterpolation
import threesixty.data.Data._
import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._

/**
  * Created by Markus on 23.01.2016.
  */
class ProcessingStrategyDeductionTest extends FunSpec{

  //creating InputData Set
  val id0 = new Identifier("testdata0")
  val measurement0 = "Heart Rate"

  val dp0_1 = new DataPoint(new Timestamp(1200), DoubleValue(85))
  val dp0_2 = new DataPoint(new Timestamp(1300), DoubleValue(100))
  val dp0_3 = new DataPoint(new Timestamp(1400), DoubleValue(125))
  val dp0_4 = new DataPoint(new Timestamp(1500), DoubleValue(85))
  val dp0_5 = new DataPoint(new Timestamp(1600), DoubleValue(100))
  val dataPoints0 = List(dp0_1,dp0_2,dp0_3,dp0_2,dp0_1)

  val timeframe0 = new Timeframe(dp0_1.timestamp, dp0_5.timestamp)
  val rel0 = Reliability.Device
  val res0 = Resolution.Middle
  val scal0 = Scaling.Ordinal
  val act0 = new ActivityType("Testing my new Device")
  act0.setDescription("short run without elan")
  val metadata0 = new CompleteInputMetadata(timeframe0,rel0,res0,scal0,act0)
  var inputData = new InputData(id0,measurement0, dataPoints0, metadata0 )






  describe("lokal tryouts") {
    it(" with Debugger Konsolenprints"){

      var set = Set(1,2,3,4)
      println("Alles: "+ set.toString())
      val head = set.head
      println("Kopf: " +head)
      println("ohne Kopf" + (set - head))
   }

   describe("Linear interpolation"){
     it("should compute the right value for a given InputData Set"){
       val testset = Set(inputData)
       val linInt = new LinearInterpolation(null, null)
       println("linInt.degreeOfFit(testset with size 1): " + linInt.degreeOfFit(testset))


     }
   }

  }
}
