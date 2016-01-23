package threesixty.algorithms

import threesixty.data.metadata.{Reliability, Resolution, Scaling}
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Identifier
import threesixty.processor.MultiProcessingMethod

import clustering._
import threesixty.visualizer.Visualization
import threesixty.visualizer.visualizations.BarChart.BarChartConfig.BarChart
import threesixty.visualizer.visualizations.HeatLineChart.HeatLineChartConfig.HeatLineChart
import threesixty.visualizer.visualizations.LineChart.LineChartConfig.LineChart
import threesixty.visualizer.visualizations.PieChart.PieChartConfig.PieChart
import threesixty.visualizer.visualizations.PolarAreaChart.PolarAreaChartConfig.PolarAreaChart
import threesixty.visualizer.visualizations.ProgressChart.ProgressChartConfig.ProgressChart
import threesixty.visualizer.visualizations.ScatterChart.ScatterChartConfig.ScatterChart
import threesixty.visualizer.visualizations.ScatterColorChart.ScatterColorChartConfig.ScatterColorChart

object Clustering {

    def byCluster[D](clustering:Map[D, Classification]):Map[Classification, Set[D]] = {
        var result:Map[Classification, Set[D]] = Map()

        def invert(kvp:(D, Classification)):Unit = {
            result += (kvp._2 -> (result.getOrElse(kvp._2, Set()) + kvp._1))
        }
        clustering.foreach(invert)

        result
    }

    trait Classification
    case class Cluster(id: Int) extends Classification
    object     Noise            extends Classification
    object     Unclassified     extends Classification


    // TODO: Review distance functions

    type DistanceFunctionSelector[D, V] = ((D => V)*) => DistanceFunction[D]

    type DistanceFunction[D] = (D, D) => Double


    def genericManhattanDistance[D, V <% Double]: DistanceFunctionSelector[D, V] = {
        selectors => {
            (d1, d2) => {
                (selectors.map {
                  s => math.abs(s(d1) - s(d2))
                }).sum
            }
        }
    }

    def manhattanDistance = genericManhattanDistance[TaggedDataPoint, Double]

    def genericEuclidianDistance[D, V <% Double]: DistanceFunctionSelector[D, V] = {
        selectors => {
            (d1, d2) => {
                math.sqrt(
                    (selectors.map {
                        s => math.pow(s(d1) - s(d2), 2)
                    }).sum
                )
            }
        }
    }

    def euclidianDistanceS = genericEuclidianDistance[TaggedDataPoint, Double]


    def dbscan[D](dataset: Set[D],
                  distFunction: DistanceFunction[D])
                 (implicit minPts: Int,
                           epsilon: Double): Map[D, Classification] =
        DBSCAN.run[D](dataset,distFunction)

}

case class Clustering(idMapping: Map[Identifier, Identifier])
    extends MultiProcessingMethod(idMapping: Map[Identifier, Identifier]) {

    def apply(dataInput: Set[ProcessedData]): Set[ProcessedData] = ??? // TODO implement



    def computeDegreeOfFit(inputData : InputData) : Double = {
        var temp = 0.0

        val meta = inputData.metadata

        if (meta.scaling == Scaling.Nominal){
            temp += 0.2 }
        else (temp += 0.1)


        if (meta.resolution == Resolution.Low ){
            temp+= 0.25}
        else if (meta.resolution == Resolution.Middle){
            temp+= 0.1}
        else {
            temp += 0.15}

        if (meta.reliability == Reliability.User){
            temp += 0.2}
        else if (meta.reliability == Reliability.Device){
            temp+= 0.1}

        if (inputData.dataPoints.length > 25){
            temp += 0.35 }
        else if (inputData.dataPoints.length >= 5) {
            temp += 0.2}

        temp
    }

    def computeDegreeOfFit(inputData: InputData, targetVisualization : Visualization ) : Double = {

        val visFactor =  targetVisualization match {
            //ideal
            case ScatterChart(_,_) => -1.0
            case ScatterColorChart(_,_) => -1.0
            //medium
            case BarChart(_,_) => 0.3
            case  PolarAreaChart(_,_) => 0.3  //equal to BarChar
            //maybe but rather bad
            case LineChart(_,_) => 0.2
            case  HeatLineChart(_,_) => 0.2
            case PieChart(_,_) => 0.2
            //bad
            case ProgressChart(_,_) => 0
            //default
            case _ => 0.3
        }


        // break option for ideal case
        if (visFactor == -1.0)
            1.0
        else {
            visFactor * computeDegreeOfFit(inputData)
        }
    }


}
