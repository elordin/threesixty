package threesixty.algorithms

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

    def pam[D] = throw new NotImplementedError


    def clarans[D] = throw new NotImplementedError


    def em[D] = throw new NotImplementedError


    def kModes[D] = throw new NotImplementedError


    def snn[D] = throw new NotImplementedError


    def dbscan[D](dataset:Set[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, Classification] =
        DBSCAN.run(dataset,distFunction)


    def hdbscan[D] = throw new NotImplementedError


    def optics[D](db:List[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, Classification] = {
        throw new NotImplementedError
    }
}
