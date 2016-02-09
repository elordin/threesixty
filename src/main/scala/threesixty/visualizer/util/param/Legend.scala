package threesixty.visualizer.util.param

import threesixty.visualizer.util.LegendPositionType.LegendPosition

/**
  * Created by Thomas on 09.02.2016.
  */
case class LegendParam(
    val position: Option[LegendPosition],
    val verticalOffset: Int,
    val horizontalOffset: Int,
    val symbolWidth: Int,
    val size: Int,
    val fontFamily: String) {

    require(size >= 0, "Value for size cannot be negative.")
}

case class OptLegendParam(
    val position: Option[String] = None,
    val verticalOffset: Option[Int] = None,
    val horizontalOffset: Option[Int] = None,
    val symbolWidth: Option[Int] = None,
    val size: Option[Int] = None,
    val fontFamily: Option[String] = None) {
}
