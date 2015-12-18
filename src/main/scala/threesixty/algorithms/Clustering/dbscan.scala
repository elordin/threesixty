package threesixty.algorithms.clustering

import threesixty.algorithms.Clustering._

object DBSCAN {
    /**
     *  DBSCAN density-based clustering algorithm by Ester, Kriegel, Sander and Xu.
     *  http://www.dbs.ifi.lmu.de/Publikationen/Papers/KDD-96.final.frame.pdf
     *
     *  Note that behavior for datapoints D that might be in two clusters depends
     *  on their order in the input Set
     *
     *  @author Thomas Weber
     *  @param dataset Set of datapoints to cluster
     *  @param distFunction Distance function
     *  @param minPts implicit min points
     *  @param epsilon implicit epsilon environment size
     *  @returns Map of Datapoint -> ClusterType
     */
    def run[D](dataset:Set[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, Classification] = {

        var dataMap:Map[D, (Boolean, Classification)] = (for (p <- dataset) yield (p -> (false, Unclassified))).toMap

        def expandCluster(point:D, ns:Set[D], cindex:Int) {
            var neighbours = ns
            dataMap += point -> (true, new Cluster(cindex))
            while (neighbours.size > 0) {
                val n = neighbours.head
                neighbours = neighbours.tail
                (dataMap get n).foreach(
                    (t:(Boolean, Classification)) => {
                        var (visited, c) = t
                        if (!visited) {
                            dataMap += (n -> (true, c))
                            val newNeighbours = rangeQuery(n)
                            if (newNeighbours.size >= minPts)
                                neighbours |= newNeighbours
                        }
                        c match {
                            case Cluster(_) => {}
                            case _ => dataMap += (n -> (true, Cluster(cindex)))
                        }
                    }
                )
            }
        }

        def rangeQuery(point:D):Set[D] = {
            dataMap.keys.filter(distFunction(point, _) < epsilon).toSet
        }

        var cindex:Int = 0
        for ((point, _) <- dataMap) {
            (dataMap get point).foreach(
                (_ match {
                    case (false, c) => {
                        dataMap += point -> (true, c)
                        val neighbours = rangeQuery(point)
                        if (neighbours.size < minPts) {
                            dataMap += point -> (true, Noise)
                        } else {
                            cindex += 1
                            expandCluster(point, neighbours, cindex)
                        }
                    }
                    case _ => {}
                })
            )
        }

        dataMap.map((kv:(D, (Boolean, Classification))) => {
                val (p, (_, c)) = kv
                (p, c)
            })
    }

}
