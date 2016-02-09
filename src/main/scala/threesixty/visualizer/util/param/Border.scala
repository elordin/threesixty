package threesixty.visualizer.util.param

case class Border(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int) {

    require(top >= 0, "Negative value for border top is not allowed.")
    require(bottom >= 0, "Negative value for border bottom is not allowed.")
    require(left >= 0, "Negative value for border left is not allowed.")
    require(right >= 0, "Negative value for border right is not allowed.")
}
