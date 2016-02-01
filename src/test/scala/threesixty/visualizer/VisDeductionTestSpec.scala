package threesixty.visualizer

import java.sql.Timestamp
import java.util.UUID

import org.scalatest.FunSpec
import threesixty.algorithms.interpolation.LinearInterpolation
import threesixty.data.Data._
import threesixty.data.{InputData, DataPoint}
import threesixty.data.metadata._
import threesixty.processor.ProcessingStep
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig

/**
  * Created by Markus on 30.01.2016.
  */
class VisDeductionTestSpec extends  FunSpec{

//creating example Data
  val identifier = UUID.randomUUID()
  val identifier1 = UUID.randomUUID()
  val measurement = "Heart Rate"

  val firstDataPoint = DataPoint(new Timestamp(1453227516719L), DoubleValue(130.3))
  val secondDataPoint = DataPoint(new Timestamp(1453227568330L), DoubleValue(128.7))
  val thirdDataPoint = DataPoint(new Timestamp(1453227593147L), DoubleValue(129.1))
  val fourthDataPoint = DataPoint(new Timestamp(1453227615119L), DoubleValue(129.5))
  val fifththDataPoint = DataPoint(new Timestamp(1453228615119L), DoubleValue(99.5))
  val dataPoints = List(firstDataPoint, secondDataPoint, thirdDataPoint, fourthDataPoint)

  val timeframe = Timeframe(new Timestamp(1453227383043L), new Timestamp(1453227461703L))
  val activityType = ActivityType("Walking")
  activityType.setDescription("Long walk with my dogs")
  val resolution = Resolution.High
  val reliability = Reliability.Device
  val scaling = Scaling.Ordinal
  val inputMetadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType)

  val inputDataSet0 = InputData(identifier.toString, measurement, dataPoints, inputMetadata)
  val inputDataSet1 = InputData(identifier1.toString, measurement, dataPoints ++ List(fifththDataPoint), inputMetadata)

  //creating example Visualizations
  val lineChart = LineChartConfig(
    ids = Set("abc", "123"),
    height = 1024,
    width = 768,
    optXMin = Some(new Timestamp(100000)),
    optXMax = Some(new Timestamp(200000)),
    optYMin = Some(10.0),
    optYMax = Some(123.456),
    _xLabel = Some("X-Axis"),
    _yLabel = Some("Y-Axis"),
    _title = Some("Title"),
    _borderTop = Some(100),
    _borderBottom = Some(50),
    _borderLeft = Some(50),
    _borderRight = Some(50),
    _minDistanceX = Some(50),
    _minDistanceY = Some(50),
    optUnitX = Some("seconds30"),
    optUnitY = Some(10.0)
  )

  val pieChart = new PieChartConfig(
    ids = Set("a"),
    height = 1024,
    width = 768,
    _title = Some("Title"),
    _borderTop = Some(100),
    _borderBottom = Some(50),
    _borderLeft = Some(50),
    _borderRight = Some(50),
   _distanceTitle = Some(50),
   _angleStart = None,
   _angleEnd = None,
   _radius = None,
   _innerRadiusPercent = None,
   _showValues = Some(true),
   _fontSizeTitle   = None,
   _fontSize  = None,
   _widthLegendSymbol = None,
   _distanceLegend = None
  )

  val linInt = new LinearInterpolation(4, null)
  val linIntStep = new ProcessingStep(linInt, null)


  val lineChartMeta = LineChartConfig.metadata
  val pieChartMeta = PieChartConfig.metadata

describe(" a given ProcessingMethod isMatching() a Visualization"){

  it("should read the Requirements correctly"){
   assert(lineChartMeta.numberOfInputs() == Int.MaxValue)
    assert(pieChartMeta.numberOfInputs() == 1)

    var lineReq = lineChartMeta.requirementList.head
    assert(lineReq.resolution == None)
    assert(lineReq.scaling == Some(Scaling.Ordinal))
    assert(lineReq.requiredProcessingMethods == None)
    assert(lineReq.excludedProcessingMethods== None)
    assert(lineReq.requiredGoal == None)

    var pieReq = pieChartMeta.requirementList.head
    assert(pieReq.resolution == None)
    assert(pieReq.scaling == None)
    assert(pieReq.excludedProcessingMethods == None)
    assert(pieReq.requiredGoal == None)
    assert(pieReq.requiredProcessingMethods == None)

  }

  it("should compute if Visualization isMatching a given ProcessingMethod and one given Dataset"){

    val ListOfInputdata = List(inputDataSet0)

    //testcase unlimited Data allowed
    val res0 = LineChartConfig.isMatching(ListOfInputdata,linIntStep)
    res0 match {
      case Some(datalist) => assert(datalist.equals(ListOfInputdata))
      case None => assert(false)
    }

    //testcase with limited data
    val res1 = PieChartConfig.isMatching(ListOfInputdata,linIntStep)
    res1 match {
      case Some(datalist) => assert(datalist.equals(ListOfInputdata))
      case None => assert(false)
    }

  }

  it("should compute whether Visualization isMatching a given PRocessingMethod and a set of Datasets"){
    val ListOfInputdata = List(inputDataSet0,inputDataSet1)

    //testcase unlimited Data allowed
    val res0 = LineChartConfig.isMatching(ListOfInputdata,linIntStep)
    res0 match {
      case Some(datalist) => assert(datalist.equals(ListOfInputdata))
      case None => assert(false)
    }

  //algorithm is aware of limit
    val res1 = PieChartConfig.isMatching(ListOfInputdata,linIntStep)
    assert(res1 == None) //because pieChart does not work on 2 InputData



  }





}//end describe



}
