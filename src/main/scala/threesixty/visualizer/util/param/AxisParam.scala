package threesixty.visualizer.util.param

import threesixty.data.Data.Timestamp

abstract class OptAxisParam(
    label: Option[String],
    labelSize: Option[Int],
    labelFontFamily: Option[String],
    minPxBetweenGridPoints: Option[Int],
    unitLabelSize: Option[Int],
    unitLabelFontFamily: Option[String],
    showGrid: Option[Boolean],
    showLabels: Option[Boolean]) {

    require(minPxBetweenGridPoints.getOrElse(1) > 0, "Value for minPxBetweenGridPoints must be greater than 0.")
}

case class OptValueAxisParam(val label: Option[String] = None,
                             val labelSize: Option[Int],
                             val labelFontFamily: Option[String] = None,
                             val min: Option[Double] = None,
                             val max: Option[Double] = None,
                             val minPxBetweenGridPoints: Option[Int] = None,
                             val unit: Option[Double] = None,
                             val unitLabelSize: Option[Int] = None,
                             val unitLabelFontFamily: Option[String] = None,
                             val showGrid: Option[Boolean] = None,
                             val showLabels: Option[Boolean] = None) extends OptAxisParam(
    label,
    labelSize,
    labelFontFamily,
    minPxBetweenGridPoints,
    unitLabelSize,
    unitLabelFontFamily,
    showGrid,
    showLabels) {

}

case class OptTimeAxisParam(val label: Option[String] = None,
                            val labelSize: Option[Int] = None,
                            val labelFontFamily: Option[String] = None,
                            val min: Option[Timestamp] = None,
                            val max: Option[Timestamp] = None,
                            val minPxBetweenGridPoints: Option[Int] = None,
                            val unit: Option[String] = None,
                            val unitLabelSize: Option[Int] = None,
                            val unitLabelFontFamily: Option[String] = None,
                            val showGrid: Option[Boolean] = None,
                            val showLabels: Option[Boolean] = None) extends OptAxisParam(
    label,
    labelSize,
    labelFontFamily,
    minPxBetweenGridPoints,
    unitLabelSize,
    unitLabelFontFamily,
    showGrid,
    showLabels) {

}
