package threesixty.algorithms

import threesixty.data.metadata.{Reliability, Resolution, Scaling}
import threesixty.data.{InputData, ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Identifier
import threesixty.processor.{MultiProcessingMethod, ProcessingMethodCompanion, ProcessingStep}

import clustering._
import threesixty.visualizer.VisualizationConfig
import threesixty.visualizer.visualizations.barChart.BarChartConfig
import threesixty.visualizer.visualizations.heatLineChart.HeatLineChartConfig
import threesixty.visualizer.visualizations.lineChart.LineChartConfig
import threesixty.visualizer.visualizations.pieChart.PieChartConfig
import threesixty.visualizer.visualizations.polarAreaChart.PolarAreaChartConfig
import threesixty.visualizer.visualizations.progressChart.ProgressChartConfig
import threesixty.visualizer.visualizations.scatterChart.ScatterChartConfig
import threesixty.visualizer.visualizations.scatterColorChart.ScatterColorChartConfig


object Clustering extends ProcessingMethodCompanion {

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


    def name = "Clustering"

    def usage = "Use responsibly..." // TODO

    def fromString: (String) => ProcessingStep = ???

    def computeDegreeOfFit(inputData: InputData): Double = {
        var temp = 0.0

        val meta = inputData.metadata

        if (meta.scaling == Scaling.Nominal) {
            temp += 0.2
        } else (temp += 0.1)

        if (meta.resolution == Resolution.Low) {
            temp+= 0.25
        } else if (meta.resolution == Resolution.Middle) {
            temp+= 0.1
        } else {
            temp += 0.15}

        if (meta.reliability == Reliability.User) {
            temp += 0.2
        } else if (meta.reliability == Reliability.Device) {
            temp+= 0.1
        }

        if (inputData.dataPoints.length > 25) {
            temp += 0.35
        }
        else if (inputData.dataPoints.length >= 5) {
            temp += 0.2
        }

        temp
    }

    def computeDegreeOfFit(inputData: InputData, targetVisualization: VisualizationConfig): Double = {

        val visFactor =  targetVisualization match {
            //ideal
            case _:ScatterChartConfig       => -1.0
            case _:ScatterColorChartConfig  => -1.0
            //medium
            case _:BarChartConfig           => 0.3
            case _:PolarAreaChartConfig     => 0.3  //equal to BarChar
            //maybe but rather bad
            case _:LineChartConfig          => 0.2
            case _:HeatLineChartConfig      => 0.2
            case _:PieChartConfig           => 0.2
            //bad
            case _:ProgressChartConfig      => 0
            //default
            case _                          => 0.3
        }


        // break option for ideal case
        if (visFactor == -1.0)
            1.0
        else {
            visFactor * computeDegreeOfFit(inputData)
        }
    }
}

case class Clustering(idMapping: Map[Identifier, Identifier])
    extends MultiProcessingMethod(idMapping: Map[Identifier, Identifier]) {

    def apply(dataInput: Set[ProcessedData]): Set[ProcessedData] = ??? // TODO implement

}
