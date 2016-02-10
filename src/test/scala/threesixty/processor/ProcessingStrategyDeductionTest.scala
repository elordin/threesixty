
package threesixty.processor

import org.scalatest.FunSpec
import threesixty.ProcessingMethods.Accumulation.Accumulation
import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.ProcessingMethods.TimeSelection.TimeSelection
import threesixty.ProcessingMethods.clustering.Clustering
import threesixty.ProcessingMethods.interpolation.LinearInterpolation
import threesixty.data.Data._
import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig

/**
  * Created by Markus Schnappinger on 23.01.2016.
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
  val act0 = new ActivityType("Testing my new Device, take 1")
  act0.setDescription("short run without elan")
  val metadata0 = new CompleteInputMetadata(timeframe0,rel0,res0,scal0,act0,dataPoints0.length)
  var inputData = new InputData(id0,measurement0, dataPoints0, metadata0 )


  //creating second InputData Set
  val id1 = new Identifier("testdata1")
  val measurement1 = "Heart Rate"

  val dp1_1 = new DataPoint(new Timestamp(3200), DoubleValue(85))
  val dp1_2 = new DataPoint(new Timestamp(3300), DoubleValue(100))
  val dp1_3 = new DataPoint(new Timestamp(3400), DoubleValue(145))
  val dp1_4 = new DataPoint(new Timestamp(3500), DoubleValue(95))
  val dp1_5 = new DataPoint(new Timestamp(3600), DoubleValue(110))
  val dataPoints1 = List(dp1_1,dp1_2,dp1_3,dp1_2,dp1_1)

  val timeframe1 = new Timeframe(dp1_1.timestamp, dp1_5.timestamp)
  val rel1 = Reliability.Device
  val res1 = Resolution.Middle
  val scal1 = Scaling.Ordinal
  val act1 = new ActivityType("Testing my new Device, take 2")
  act1.setDescription("short run with elan")
  val metadata1 = new CompleteInputMetadata(timeframe1,rel1,res1,scal1,act1,dataPoints1.length)
  var inputData1 = new InputData(id1,measurement1, dataPoints1, metadata1 )

  //varying metadata Resolution
  val id2 = new Identifier("testdata2")
  val metadata2 = new CompleteInputMetadata(timeframe1,rel1,Resolution.Low,scal1,act1,dataPoints1.length)
  var inputData2 = new InputData(id2,measurement1, dataPoints1, metadata2 )


  //constants used as test parameters
  val lineChart = LineChartConfig(Seq.empty, 768, 1024)
  val barChart = BarChartConfig(Seq.empty, 768, 1024)
  val pieChart = PieChartConfig(Seq.empty, 768, 1024)
  val scatter = ScatterChartConfig(Seq.empty, 768, 1024)


  //unfortunately, scala arithmethic causes standard implemenation to fail
  // because of errors like "0.56 does not equal 0.559999999999999".
  //this is why a wrapper is used and may look dirty

   describe("Linear interpolation"){
     it("should compute the right value for a given Single InputData"){

         val testset = inputData
         val result = 0.4 + 0.2 + 0.1  //explaination: 0.4 <= ordinal scaling, 0.2 <= enough values to interpolate, 0.1 middle resolution

         assert((LinearInterpolation.degreeOfFit(testset) == (result)))
     }

     it (" InputData SET, but same metadata"){

         val testset = Seq(inputData,inputData1)
         val result = 0.4 + 0.2 + 0.1 //same as for one single set, as both has same metadata

         assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(testset: _*) - result))
     }

     it (" InputData SET, various metadata"){

       val testset = Seq(inputData,inputData1,inputData2)
       val result = 0.6 //value for InputData with Resolution.Low

       assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(testset: _*) - result))
     }

   it ("InputData + given VisualizationType"){

        val testset = Seq(inputData)

        assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(barChart,testset: _*) - (0.7 * 0.8)))
        assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(pieChart,testset: _*) - (0.7 * 0.0)))
        assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(lineChart,testset: _*) - (0.7 * 1.0)))
        assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(scatter,testset: _*) - (0.7 * 0.2)))

   }

     it("InputData SET + given VisualizationType"){

       val testset = Seq(inputData, inputData1, inputData2)

       assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(barChart, testset: _*) - (0.6 * 0.8)))
       assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(pieChart, testset: _*) - (0.6 * 0.0)))
       assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(lineChart, testset: _*) - (0.6 * 1.0)))
       assert(0.0001 > math.abs(LinearInterpolation.degreeOfFit(scatter, testset: _*) - (0.6 * 0.2)))

     }

  }


  describe("Clustering") {
    it("should compute the right value for a given Single InputData"){

      val testset = (inputData)
      val result = 0.1 + 0.1 + 0.1 + 0.2

      assert((Clustering.degreeOfFit(testset) == (result)))
    }

    it (" InputData SET, but same metadata"){

      val testset = Seq(inputData,inputData1)
      val result = 0.1 + 0.1 + 0.1 + 0.2 //same as for one single set, as both has same metadata

      assert(0.0001 > math.abs(Clustering.degreeOfFit(testset: _*) - result))
    }

    it (" InputData SET, various metadata"){

      val testset = Seq(inputData,inputData1,inputData2)
      val result = 0.5 //value for InputData with Resolution.Middle | LOW => 0.65

      assert(0.0001 > math.abs(Clustering.degreeOfFit(testset: _*) - result))
  }

    it ("InputData + given VisualizationType"){

      val testset = Seq(inputData)

      assert(0.0001 > math.abs(Clustering.degreeOfFit(barChart, testset: _*) - (0.5 * 0.3)))
      assert(0.0001 > math.abs(Clustering.degreeOfFit(pieChart, testset: _*) - (0.5 * 0.2)))
      assert(0.0001 > math.abs(Clustering.degreeOfFit(lineChart, testset: _*) - (0.5 * 0.2)))

      assert((Clustering.degreeOfFit(scatter, testset: _*)==1))

    }

    it("InputData SET + given VisualizationType"){

      val testset = Seq(inputData, inputData1, inputData2)

      assert(0.0001 > math.abs(Clustering.degreeOfFit(barChart, testset: _*) - (0.5 * 0.3)))
      assert(0.0001 > math.abs(Clustering.degreeOfFit(pieChart, testset: _*) - (0.5 * 0.2)))
      assert(0.0001 > math.abs(Clustering.degreeOfFit(lineChart, testset: _*) - (0.5 * 0.2)))

      assert((Clustering.degreeOfFit(scatter, testset: _*)==1))
    }

  }

  /*
  /* //TODO
   this test has to be adapted, as soon as someone has changed parameter in computeDegreeOfFit for any of the methods*/
  describe("deduce best-possible ProcessingStrategy"){
    it("should do this for no given Vis"){
      val inputdataSet= Set(inputData)
      val processor = new Processor         with LinearInterpolation.Mixin
        with Aggregation.Mixin
        with Accumulation.Mixin
        with TimeSelection.Mixin

      val deducedProcessingStrategy0 = processor.deduce(inputdataSet).steps.map(_.method.companion).head
      val dedicatedProcessingStrategy0 = LinearInterpolation

      assert(deducedProcessingStrategy0 equals dedicatedProcessingStrategy0)

      val processor1 = new Processor with Accumulation.Mixin
        with Aggregation.Mixin
        with LinearInterpolation.Mixin
        with TimeSelection.Mixin
      val dedicatedProcessingStrategy1 = Accumulation
      val deducedProcessingStreategy1 = processor1.deduce(inputdataSet).steps.map(_.method.companion).head

      assert(dedicatedProcessingStrategy1 equals deducedProcessingStreategy1)

    }

    it("should do this for a given Vis"){
      val inputdataSet= Set(inputData)
      val processor = new Processor         with LinearInterpolation.Mixin
        with Aggregation.Mixin
        with Accumulation.Mixin
        with TimeSelection.Mixin

      val deducedProcessingStrategy0 = processor.deduce(inputdataSet).steps.map(_.method.companion).head
      val dedicatedProcessingStrategy0 = LinearInterpolation

      assert(deducedProcessingStrategy0 equals dedicatedProcessingStrategy0)

      val LinValue = LinearInterpolation.computeDegreeOfFit(inputData,lineChart)
      val aggValue = Aggregation.computeDegreeOfFit(inputData,lineChart)
      val accValue = Accumulation.computeDegreeOfFit(inputData,lineChart)
      val tiSelValue = TimeSelection.computeDegreeOfFit(inputData, lineChart)
      println("LinValue: " + LinValue)
      println("aggValue: " + aggValue)
      println("accValue: " + accValue)
      println("tiSelValue: " + tiSelValue)




    }
  }*/



}


