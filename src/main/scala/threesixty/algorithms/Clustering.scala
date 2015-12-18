package threesixty.algorithms

object Clustering {

    protected trait ClusterType
    case class Cluster(id: Int) extends ClusterType
    object     Noise            extends ClusterType
    object     Unclassified     extends ClusterType

    def pam[D] = throw new NotImplementedError


    def clarans[D] = throw new NotImplementedError


    def em[D] = throw new NotImplementedError


    def kModes[D] = throw new NotImplementedError


    def snn[D] = throw new NotImplementedError

    /**
     *  DBSCAN density-based clustering algorithm by Ester, Kriegel, Sander and Xu.
     *  http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.71.1980
     *  @author Thomas Weber
     *  @param dataset Set of datapoints to cluster
     *  @param distFunction Distance function
     *  @param minPts implicit min points
     *  @param epsilon implicit epsilon environment size
     *  @returns Map of Datapoint -> ClusterType
     */
    def dbscan[D](dataset:Set[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, ClusterType] = {

        var dataMap:Map[T, (Boolean, Classification)] = (for (p <- dataset) yield (p -> (false, Unclassified))).toMap

        def expandCluster(point:T, ns:Set[T], cindex:Int) {
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

        def rangeQuery(point:T):Set[T] = {
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

        dataMap.map((kv:(T, (Boolean, Classification))) => {
                val (p, (_, c)) = kv
                (p, c)
            })
    }


    def hdbscan[D] = throw new NotImplementedError


    def optics[D](db:List[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, ClusterType] = {
        throw new NotImplementedError
    }
}
