package threesixty.algorithms.clustering

import threesixty.algorithms.Clustering._

private[algorithms] object OPTICS {
    /**
     *  OPTICS clustering algorithm by Ankerst, Breunig, Kriegel and Sander.
     *  http://www.dbs.ifi.lmu.de/Publikationen/Papers/OPTICS.pdf
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
        throw new NotImplementedError
    }
}
