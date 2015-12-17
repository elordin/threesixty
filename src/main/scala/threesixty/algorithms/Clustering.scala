package threesixty.algorithms

object Clustering {

    trait ClusterType
    case class Cluster(id: Int) extends ClusterType
    object     Noise            extends ClusterType

    def pam[D] = throw new NotImplementedError


    def clarans[D] = throw new NotImplementedError


    def em[D] = throw new NotImplementedError


    def kModes[D] = throw new NotImplementedError


    def snn[D] = throw new NotImplementedError


    def dbscan[D](db:List[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, ClusterType] = {
        throw new NotImplementedError
    }


    def hdbscan[D] = throw new NotImplementedError


    def optics[D](db:List[D],
                  distFunction:(D, D) => Double)
                 (implicit minPts:Int,
                           epsilon:Double):Map[D, ClusterType] = {
        throw new NotImplementedError
    }
}
