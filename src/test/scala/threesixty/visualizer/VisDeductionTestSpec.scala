package threesixty.visualizer

import java.sql.Timestamp
import java.util.UUID

import org.scalatest.FunSpec
import threesixty.ProcessingMethods.Accumulation.Accumulation
import threesixty.ProcessingMethods.Aggregation.Aggregation
import threesixty.ProcessingMethods.TimeSelection.TimeSelection
import threesixty.ProcessingMethods.interpolation.LinearInterpolation
import threesixty.data.Data._
import threesixty.data.{DataPool, InputData, DataPoint, InputDataSkeleton}
import threesixty.data.metadata._
import threesixty.engine.VisualizationEngine
import threesixty.persistence.FakeDatabaseAdapter
import threesixty.processor.{ProcessingStrategy, Processor, ProcessingStep}
import threesixty.visualizer.util.Border
import threesixty.visualizer.visualizations.{scatterChart, barChart, pieChart, lineChart}
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations._

import scala.xml.Elem

/**
* Created by Markus on 30.01.2016.
*/
class VisDeductionTestSpec extends  FunSpec {

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
    val inputMetadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType, dataPoints.length)

    val inputDataSkeleton0 = new InputDataSkeleton("data1", measurement, inputMetadata)
    val inputDataSkeleton1 = new InputDataSkeleton("data2", measurement, inputMetadata.copy(size = dataPoints.length + 1))

    val inputDataSet0 = inputDataSkeleton0.fill(List(firstDataPoint, secondDataPoint, thirdDataPoint, fourthDataPoint))
    val inputDataSet1 = inputDataSkeleton1.fill(List(firstDataPoint, secondDataPoint, thirdDataPoint, fourthDataPoint, fifththDataPoint))

    //creating example Visualizations
    val lineChart = LineChartConfig(
        ids = Seq("abc", "123"),
        height = 1024,
        width = 768,
        _xMin = Some(new Timestamp(100000)),
        _xMax = Some(new Timestamp(200000)),
        _yMin = Some(10.0),
        _yMax = Some(123.456),
        _xLabel = Some("X-Axis"),
        _yLabel = Some("Y-Axis"),
        _title = Some("Title"),
        _border = Some(Border(100,50,50,50)),
        _minPxBetweenXGridPoints = Some(50),
        _minPxBetweenYGridPoints = Some(50),
        _xUnit = Some("seconds30"),
        _yUnit = Some(10.0)
    )

    val pieChart = new PieChartConfig(
        ids = Seq("a"),
        height = 1024,
        width = 768,
        _title = Some("Title"),
        _border = Some(Border(100,50,50,50)),
        _titleVerticalOffset = Some(50),
        _showValues = Some(true)
    )
    //val dataPool = new DataPool(Seq(inputDataSkeleton0), FakeDatabaseAdapter)
    val dataPool = new DataPool(Seq(inputDataSet0), FakeDatabaseAdapter)
    val idMap = dataPool.skeletons.map({ skeleton => (skeleton.id, skeleton.id) }).toMap

    val linInt = new LinearInterpolation(4,idMap)
    val linIntStep = linInt.asProcessingStep


    val lineChartMeta = LineChartConfig.metadata
    val pieChartMeta = PieChartConfig.metadata

    describe(" a given ProcessingMethod isMatching() a Visualization") {
        it("should read the Requirements correctly") {
            assert(lineChartMeta.numberOfInputs() == Int.MaxValue)
            assert(pieChartMeta.numberOfInputs() == 1)

            var lineReq = lineChartMeta.requirementList.head
            assert(lineReq.resolution == None)
            assert(lineReq.scaling == Some(Scaling.Ordinal))
            assert(lineReq.requiredProcessingMethods == None)
            assert(lineReq.excludedProcessingMethods == None)

            var pieReq = pieChartMeta.requirementList.head
            assert(pieReq.resolution == None)
            assert(pieReq.scaling == None)
            assert(pieReq.excludedProcessingMethods == None)
            assert(pieReq.requiredProcessingMethods == Some(List(Aggregation)))
        }

        it("should compute if Visualization isMatching a given ProcessingMethod and one given Dataset") {

            //testcase unlimited Data allowed
            val res0 = LineChartConfig.isMatching(linIntStep, inputDataSet0)

            assert(res0.isDefined)
            assert(res0.get.equals(Seq(inputDataSet0)))

            //testcase with limited data
            val res1 = PieChartConfig.isMatching(linIntStep, inputDataSet0)
            assert(res1.isDefined)
            assert(res1.get.equals(Seq(inputDataSet0)))
        }

        it("should compute whether Visualization isMatching a given ProcessingMethod and a set of Datasets") {
            //testcase unlimited Data allowed
            val res0 = LineChartConfig.isMatching(linIntStep, inputDataSet0, inputDataSet1)
            assert(res0.isDefined)
            assert(res0.get.equals(Seq(inputDataSet0, inputDataSet1)))

            //algorithm is aware of limit
            val res1 = PieChartConfig.isMatching(linIntStep, inputDataSet0, inputDataSet1)
            assert(!res1.isDefined) //because pieChart does not work on 2 InputData
        }

        it("should compute MissingMethods for a given ProcessingStrategy"){
            val requirement = PieChartConfig.metadata.requirementList.head
            val wrongStrategy = ProcessingStrategy(linIntStep)

            assert(requirement.missingMethods(null) equals requirement.requiredProcessingMethods)
            assert(requirement.missingMethods(wrongStrategy) equals requirement.requiredProcessingMethods)
        }
    }

    describe("Deduction Algorithms") {

        val visEngine = VisualizationEngine using
        new Processor   with LinearInterpolation.Mixin
        with Aggregation.Mixin
        with Accumulation.Mixin
        with TimeSelection.Mixin and new Visualizer
        with threesixty.visualizer.visualizations.lineChart.Mixin
        with threesixty.visualizer.visualizations.pieChart.Mixin
        with barChart.Mixin
        with scatterChart.Mixin and FakeDatabaseAdapter

        val dataPool = new DataPool(Seq(inputDataSkeleton0), FakeDatabaseAdapter)
        val idMap = dataPool.skeletons.map({ skeleton => (skeleton.id, skeleton.id) }).toMap

        val strategy = ProcessingStrategy(visEngine.processor.processingInfos.values.head.default(idMap))


        it("deduction of a Vis, given a ProcStrat") {
            val deduced = visEngine.visualizer.deduce(strategy, inputDataSkeleton0)
            // assert(deduced._1.steps.head.method.companion.name equals "Linear Interpolation")

            // println(deduced._2.getClass) //assert not yet possible, as long as Parameters in Processing deductions aren't adapted. now(4.2.16) all are the same as in LineaerInterpolation
            //assert (deducted._2 equals  lineChart   ) //shall be lineChartConfig
            println(deduced.getClass equals lineChart.getClass   )
        }

        it("deduction of a ProcStrat, given a Vis") {
            //is tested in ProcessingStrategyDeductionTest. can be tested not before jens has actualized his values
            val deducedProcessingStrategy0 = visEngine.processor
                .deduce(inputDataSet0).steps.map(_.method.companion).head
            val dedicatedProcessingStrategy0 = LinearInterpolation

           ///assert(deducedProcessingStrategy0 equals dedicatedProcessingStrategy0)
            println( "__Deduction of ProcStrat given Vis (LinearInterpolation) => VisStrategy: " + deducedProcessingStrategy0.getClass)

        }

        it("deduction of both ProcStrat and Vis") {
            val deducted = visEngine.deduceVisAndProc(inputDataSkeleton0)
            println( "__Deduction of Both => ProcStrategy: " + deducted._1.steps.head.method.companion.name)
            println( "__Deduction of Both => VisStrategy: " + deducted._2.getClass)
        }

    }

}
