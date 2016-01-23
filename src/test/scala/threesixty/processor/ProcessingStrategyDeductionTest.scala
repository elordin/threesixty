package threesixty.processor

import org.scalatest.FunSpec
import threesixty.algorithms.Clustering
import threesixty.algorithms.interpolation.LinearInterpolation
import threesixty.data.Data._
import threesixty.data.{DataPoint, InputData}
import threesixty.data.metadata._
import threesixty.visualizer.visualizations.BarChart.BarChartConfig.BarChart
import threesixty.visualizer.visualizations.LineChart.LineChartConfig.LineChart
import threesixty.visualizer.visualizations.PieChart.PieChartConfig.PieChart
import threesixty.visualizer.visualizations.ScatterChart.ScatterChartConfig.ScatterChart

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
  val metadata0 = new CompleteInputMetadata(timeframe0,rel0,res0,scal0,act0)
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
  val metadata1 = new CompleteInputMetadata(timeframe1,rel1,res1,scal1,act1)
  var inputData1 = new InputData(id1,measurement1, dataPoints1, metadata1 )

  //varying metadata Resolution
  val id2 = new Identifier("testdata2")
  val metadata2 = new CompleteInputMetadata(timeframe1,rel1,Resolution.Low,scal1,act1)
  var inputData2 = new InputData(id2,measurement1, dataPoints1, metadata2 )


  //constants used as test parameters
  val lineChart = new LineChart(null, null)
  val barChart = new BarChart(null,null)
  val pieChart = new PieChart(null, null)
  val scatter = new ScatterChart(null,null)

  val linInt = new LinearInterpolation(10, null)
  val clustering = new Clustering(null)




  //unfortunately, scala arithmethic causes standard implemenation to fail
  // because of errors like "0.56 does not equal 0.559999999999999".
  //this is why a wrapper is used and may look dirty

   describe("Linear interpolation"){
     it("should compute the right value for a given Single InputData"){

         val testset = Set(inputData)
         val result = 0.4 + 0.2 + 0.1  //explaination: 0.4 <= ordinal scaling, 0.2 <= enough values to interpolate, 0.1 middle resolution

         assert((linInt.degreeOfFit(testset) == (result)))
     }

     it (" InputData SET, but same metadata"){

         val testset = Set(inputData,inputData1)
         val result = 0.4 + 0.2 + 0.1 //same as for one single set, as both has same metadata

         assert(0.0001 > math.abs(linInt.degreeOfFit(testset) - result))
     }

     it (" InputData SET, various metadata"){

       val testset = Set(inputData,inputData1,inputData2)
       val result = 0.6 //value for InputData with Resolution.Low

       assert(0.0001 > math.abs(linInt.degreeOfFit(testset) - result))
     }

   it ("InputData + given VisualizationType"){

        val testset = Set(inputData)

        assert(0.0001 > math.abs(linInt.degreeOfFit(testset,barChart) - (0.7 * 0.8)))
        assert(0.0001 > math.abs(linInt.degreeOfFit(testset,pieChart) - (0.7 * 0.0)))
        assert(0.0001 > math.abs(linInt.degreeOfFit(testset,lineChart) - (0.7 * 1.0)))
        assert(0.0001 > math.abs(linInt.degreeOfFit(testset,scatter) - (0.7 * 0.2)))

   }

     it("InputData SET + given VisualizationType"){

       val testset = Set(inputData, inputData1, inputData2)

       assert(0.0001 > math.abs(linInt.degreeOfFit(testset,barChart) - (0.6 * 0.8)))
       assert(0.0001 > math.abs(linInt.degreeOfFit(testset,pieChart) - (0.6 * 0.0)))
       assert(0.0001 > math.abs(linInt.degreeOfFit(testset,lineChart) - (0.6 * 1.0)))
       assert(0.0001 > math.abs(linInt.degreeOfFit(testset,scatter) - (0.6 * 0.2)))

     }

  }


  describe("Clustering") {
    it("should compute the right value for a given Single InputData"){

      val testset = Set(inputData)
      val result = 0.1 + 0.1 + 0.1 + 0.2

      assert((clustering.degreeOfFit(testset) == (result)))
    }

    it (" InputData SET, but same metadata"){

      val testset = Set(inputData,inputData1)
      val result = 0.1 + 0.1 + 0.1 + 0.2 //same as for one single set, as both has same metadata

      assert(0.0001 > math.abs(clustering.degreeOfFit(testset) - result))
    }

    it (" InputData SET, various metadata"){

      val testset = Set(inputData,inputData1,inputData2)
      val result = 0.5 //value for InputData with Resolution.Middle | LOW => 0.65

      assert(0.0001 > math.abs(clustering.degreeOfFit(testset) - result))
  }

    it ("InputData + given VisualizationType"){

      val testset = Set(inputData)

      assert(0.0001 > math.abs(clustering.degreeOfFit(testset,barChart) - (0.5 * 0.3)))
      assert(0.0001 > math.abs(clustering.degreeOfFit(testset,pieChart) - (0.5 * 0.2)))
      assert(0.0001 > math.abs(clustering.degreeOfFit(testset,lineChart) - (0.5 * 0.2)))

      assert((clustering.degreeOfFit(testset,scatter)==1))

    }

    it("InputData SET + given VisualizationType"){

      val testset = Set(inputData, inputData1, inputData2)

      assert(0.0001 > math.abs(clustering.degreeOfFit(testset,barChart) - (0.5 * 0.3)))
      assert(0.0001 > math.abs(clustering.degreeOfFit(testset,pieChart) - (0.5 * 0.2)))
      assert(0.0001 > math.abs(clustering.degreeOfFit(testset,lineChart) - (0.5 * 0.2)))

      assert((clustering.degreeOfFit(testset,scatter)==1))
    }

  }

}

