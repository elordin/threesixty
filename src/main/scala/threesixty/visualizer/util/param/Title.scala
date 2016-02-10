package threesixty.visualizer.util.param

/**
  * Created by Thomas on 09.02.2016.
  */
case class TitleParam(
    val title: String,
    val position: PositionType.Position,
    val verticalOffset: Int,
    val horizontalOffset: Int,
    val size: Int,
    val fontFamily: String,
    val alignment: String) {

    require(size >= 0, "Value for size cannot be negative.")
}

case class OptTitleParam(
    val title: Option[String] = None,
    val position: Option[String] = None,
    val verticalOffset: Option[Int] = None,
    val horizontalOffset: Option[Int] = None,
    val size: Option[Int] = None,
    val fontFamily: Option[String] = None,
    val alignment: Option[String] = None) {

}
