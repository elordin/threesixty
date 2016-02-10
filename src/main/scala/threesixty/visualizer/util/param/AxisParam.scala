package threesixty.visualizer.util.param

import threesixty.data.Data.Timestamp

case class AxisParam(
    val label: String,
    val labelSize: Int,
    val labelFontFamily: String,
    val minPxBetweenGridPoints: Int,
    val unitLabelSize: Int,
    val unitLabelFontFamily: String,
    val showGrid: Boolean,
    val showLabels: Boolean,
    val arrowSize: Int,
    val arrowFilled: Boolean) {

    require(minPxBetweenGridPoints > 0, "Value for minPxBetweenGridPoints must be greater than 0.")
}

abstract class OptAxisParam(
    val label: Option[String],
    val labelSize: Option[Int],
    val labelFontFamily: Option[String],
    val minPxBetweenGridPoints: Option[Int],
    val unitLabelSize: Option[Int],
    val unitLabelFontFamily: Option[String],
    val showGrid: Option[Boolean],
    val showLabels: Option[Boolean],
    val arrowSize: Option[Int],
    val arrowFilled: Option[Boolean]) {

}

case class OptValueAxisParam(override val label: Option[String] = None,
                             override val labelSize: Option[Int],
                             override val labelFontFamily: Option[String] = None,
                             val min: Option[Double] = None,
                             val max: Option[Double] = None,
                             override val minPxBetweenGridPoints: Option[Int] = None,
                             val unit: Option[Double] = None,
                             override val unitLabelSize: Option[Int] = None,
                             override val unitLabelFontFamily: Option[String] = None,
                             override val showGrid: Option[Boolean] = None,
                             override val showLabels: Option[Boolean] = None,
                             override val arrowSize: Option[Int] = None,
                             override val arrowFilled: Option[Boolean] = None) extends OptAxisParam(
    label,
    labelSize,
    labelFontFamily,
    minPxBetweenGridPoints,
    unitLabelSize,
    unitLabelFontFamily,
    showGrid,
    showLabels,
    arrowSize,
    arrowFilled) {

}

case class OptTimeAxisParam(override val label: Option[String] = None,
                            override val labelSize: Option[Int] = None,
                            override val labelFontFamily: Option[String] = None,
                            val min: Option[Timestamp] = None,
                            val max: Option[Timestamp] = None,
                            override val minPxBetweenGridPoints: Option[Int] = None,
                            val unit: Option[String] = None,
                            override val unitLabelSize: Option[Int] = None,
                            override val unitLabelFontFamily: Option[String] = None,
                            override val showGrid: Option[Boolean] = None,
                            override val showLabels: Option[Boolean] = None,
                            override val arrowSize: Option[Int] = None,
                            override val arrowFilled: Option[Boolean] = None) extends OptAxisParam(
    label,
    labelSize,
    labelFontFamily,
    minPxBetweenGridPoints,
    unitLabelSize,
    unitLabelFontFamily,
    showGrid,
    showLabels,
    arrowSize,
    arrowFilled) {

}
