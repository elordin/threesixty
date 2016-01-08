package threesixty.algorithms

object TrendAnalysis {

    abstract class Trend(strength: Double)

    case class UpwardsTrend(strength: Double) extends Trend(strength) {
        require(strength > 0, "A falling trend cannot be an upward trend.")
    }

    case class DownwardTrend(strength: Double) extends Trend(strength) {
        require(strength < 0, "A rising trend cannot be an downward trend.")
    }

}
