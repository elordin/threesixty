package threesixty.data

import threesixty.data.tags.{Tag}
import Data.{Timestamp, ValueType, Identifier}

import scala.annotation.tailrec


case class TaggedDataPoint(
    val timestamp: Timestamp,
    val value: ValueType,
    val tags:Set[Tag]
)


case class ProcessedData(val id: Identifier, val dataPoints: List[TaggedDataPoint]) {
    type TDP = TaggedDataPoint

    require(dataPoints.length > 0, "Empty dataset not allowed.")

    /**
     *  Cross Join using Nested Loop Join
     */
    def join(that: ProcessedData): List[(TDP, TDP)] =
        this.dataPoints.flatMap { dp1 => that.dataPoints.map { dp2 => (dp1, dp2) } }

    def join(that: ProcessedData, predicate: (TDP, TDP) => Boolean): List[(TDP, TDP)] =
        join(that) filter predicate.tupled

    /**
     *  Hash Join
     */
    def equiJoin[T](that: ProcessedData, selector: (TDP) => T): List[(TDP, TDP)] = {
        val hashed = this.dataPoints.map { dp => (selector(dp), dp) } toMap
        @tailrec
        def filterAndTuple(dataPoints: List[TDP], init: List[(TDP, TDP)]): List[(TDP, TDP)] = dataPoints match {
            case Nil => init
            case (hd :: tl) => filterAndTuple(tl, hashed
                .get(selector(hd))
                .map({ dp: TDP => (dp, hd) :: init })
                .getOrElse(init))
        }
        filterAndTuple(that.dataPoints, Nil)
    }
}
