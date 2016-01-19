package threesixty.algorithms

import threesixty.data.{ProcessedData, TaggedDataPoint}
import threesixty.data.Data.Identifier
import threesixty.processor.MultiProcessingMethod

import clustering._

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

}
