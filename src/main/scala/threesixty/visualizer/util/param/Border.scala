package threesixty.visualizer.util.param

/**
 * Class to provide a border.
 *
 * @param top border to the top
 * @param bottom border to the bottom
 * @param left border to the left
 * @param right border to the right
 *
 * @author Thomas Engel
 */
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

/**
 * Class to provide a border but all parameters are optional.
 *
 * @param top border to the top
 * @param bottom border to the bottom
 * @param left border to the left
 * @param right border to the right
 *
 * @author Thomas Engel
 */
case class OptBorder(
    val top: Option[Int] = None,
    val bottom: Option[Int] = None,
    val left: Option[Int] = None,
    val right: Option[Int] = None) {

}
