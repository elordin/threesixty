package threesixty.algorithms

import org.scalatest._

import Clustering._


class DBSCANSpec extends FlatSpec {
    /*
     *  The results tested against are taken from the KDD I tutorial slides.
     *  http://www.dbs.ifi.lmu.de/Lehre/KDD/SS15/uebung/Tutorial03.pdf
     */

    case class Point(val x:Int, val y:Int)

    def manhattanDist(p1:Point, p2:Point):Double = {
        scala.math.abs(p1.x - p2.x) + scala.math.abs(p1.y - p2.y)
    }

    val dataset = Set[Point](Point(1,1), Point(2,1), Point(1,2), Point(2,2),
                      Point(3,5), Point(3,9), Point(3,10), Point(4,10),
                      Point(4,11), Point(5,10), Point(10,9), Point(10,6),
                      Point(9,5), Point(10,5), Point(11,5), Point(9,4),
                      Point(10,4), Point(11,4), Point(10,3), Point(7,10))

    "DBSCAN" should "return the correct clustering (epsilon = 1.1, min-pts = 2)" in {
        implicit val epsilon:Double = 1.1
        implicit val minPts:Int = 2

        val desiredResult = Map[Point, Classification](
                Point(10,3) -> Cluster(1), Point(10,5) -> Cluster(1),
                Point(11,5) -> Cluster(1), Point(11,4) -> Cluster(1),
                Point(10,6) -> Cluster(1), Point(10,4) -> Cluster(1),
                Point(9,4)  -> Cluster(1), Point(9,5)  -> Cluster(1),
                Point(3,9)  -> Cluster(2), Point(3,10) -> Cluster(2),
                Point(4,10) -> Cluster(2), Point(5,10) -> Cluster(2),
                Point(4,11) -> Cluster(2), Point(1,1)  -> Cluster(3),
                Point(2,2)  -> Cluster(3), Point(1,2)  -> Cluster(3),
                Point(2,1)  -> Cluster(3), Point(10,9) -> Noise,
                Point(3,5)  -> Noise,      Point(7,10) -> Noise)
        val dbscanResult = dbscan(dataset, manhattanDist)
        val desiredByCluster:Map[Classification, Set[Point]] = byCluster[Point](desiredResult)
        val dbscanByCluster:Map[Classification, Set[Point]]  = byCluster[Point](dbscanResult)

        assert(desiredByCluster.values.forall(dbscanByCluster.values.toSeq.contains))
        assert(dbscanByCluster.values.forall(desiredByCluster.values.toSeq.contains))
        assert(dbscanByCluster.getOrElse(Noise, Set[Point]()) == desiredByCluster.getOrElse(Noise, Set[Point]()))
    }

    it should  "return the correct clustering (epsilon = 1.1, min-pts = 4)" in {
        implicit val epsilon:Double = 1.1
        implicit val minPts:Int = 4

        val desiredResult = Map[Point, Classification](
                Point(9,4)  -> Cluster(1), Point(9,5)  -> Cluster(1),
                Point(10,3) -> Cluster(1), Point(10,4) -> Cluster(1),
                Point(10,5) -> Cluster(1), Point(10,6) -> Cluster(1),
                Point(11,4) -> Cluster(1), Point(11,5) -> Cluster(1),
                Point(3,10) -> Cluster(2), Point(4,10) -> Cluster(2),
                Point(4,11) -> Cluster(2), Point(5,10) -> Cluster(2),
                Point(1,1)  -> Noise, Point(1,2)  -> Noise,
                Point(2,1)  -> Noise, Point(2,2)  -> Noise,
                Point(3,5)  -> Noise, Point(3,9)  -> Noise,
                Point(7,10) -> Noise, Point(10,9) -> Noise
            )
        val dbscanResult = dbscan(dataset, manhattanDist)
        val desiredByCluster:Map[Classification, Set[Point]] = byCluster[Point](desiredResult)
        val dbscanByCluster:Map[Classification, Set[Point]]  = byCluster[Point](dbscanResult)

        assert(desiredByCluster.values.forall(dbscanByCluster.values.toSeq.contains))
        assert(dbscanByCluster.values.forall(desiredByCluster.values.toSeq.contains))
        assert(dbscanByCluster.getOrElse(Noise, Set[Point]()) == desiredByCluster.getOrElse(Noise, Set[Point]()))
    }


    it should "return the correct clustering (epsilon = 2.1, min-pts = 4)" in {
        implicit val epsilon:Double = 2.1
        implicit val minPts:Int = 4

        val desiredResult = Map[Point, Classification](
                Point(9,4)  -> Cluster(1), Point(9,5)  -> Cluster(1),
                Point(10,3) -> Cluster(1), Point(10,4) -> Cluster(1),
                Point(10,5) -> Cluster(1), Point(10,6) -> Cluster(1),
                Point(11,4) -> Cluster(1), Point(11,5) -> Cluster(1),
                Point(3,9)  -> Cluster(2), Point(3,10) -> Cluster(2),
                Point(4,10) -> Cluster(2), Point(4,11) -> Cluster(2),
                Point(5,10) -> Cluster(2), Point(7,10) -> Cluster(2),
                Point(1,1)  -> Cluster(3), Point(1,2)  -> Cluster(3),
                Point(2,1)  -> Cluster(3), Point(2,2)  -> Cluster(3),
                Point(3,5)  -> Noise, Point(10,9) -> Noise
            )
        val dbscanResult = dbscan(dataset, manhattanDist)
        val desiredByCluster:Map[Classification, Set[Point]] = byCluster[Point](desiredResult)
        val dbscanByCluster:Map[Classification, Set[Point]]  = byCluster[Point](dbscanResult)

        assert(desiredByCluster.values.forall(dbscanByCluster.values.toSeq.contains))
        assert(dbscanByCluster.values.forall(desiredByCluster.values.toSeq.contains))
        assert(dbscanByCluster.getOrElse(Noise, Set[Point]()) == desiredByCluster.getOrElse(Noise, Set[Point]()))
    }

}
