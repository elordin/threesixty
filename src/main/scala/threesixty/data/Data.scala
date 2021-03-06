package threesixty.data

import java.text.SimpleDateFormat

import java.sql.{Timestamp => JSQLTimestamp}

import threesixty.visualizer.util.param._

import spray.json._

import scala.util.{Failure, Success, Try}


object Data {

    type Identifier = String
    type Timestamp  = JSQLTimestamp

    trait ValueType {
        def value: Double

        override def toString: String = value.toString
    }

    case class IntValue(_value: Int) extends ValueType {
        def value: Double = _value
    }
    implicit def intValue2Double(iValue: IntValue): Double = iValue.value
    implicit def int2IntValue(value: Int): IntValue = IntValue(value)


    case class DoubleValue(val value: Double) extends ValueType
    implicit def doubleValue2Double(dValue: DoubleValue): Double = dValue.value
    implicit def double2DoubleValue(value: Double): DoubleValue = DoubleValue(value)


    /**
     *  Parent for all Enumeration Values
     *
     *  @example {{{
     *    object MoodEnum extends DataEnum {
     *        val HAPPY = new EnumValue(0, "Happy", 5.0)
     *        val SAD   = new EnumValue(1, "Sad", 1.0)
     *    }
     *  }}}
     */
    abstract class DataEnum extends Enumeration {
        /**
         *  Wrapper for standard enum values with ValueType trait and thus
         *  a double representation
         */
        protected case class EnumValue(
            val i: Int,
            val name: String,
            val value: Double
        ) extends Val(i: Int, name: String) with ValueType

        implicit def enumValue2Double(eValue: EnumValue): Double = eValue.value

        @throws[NoSuchElementException]("if not corresponding enum exists")
        implicit def double2EnumValue(value: Double): EnumValue

    }

}


object Implicits {
    import Data.Timestamp

    implicit def timestamp2Long(timestamp: Timestamp): Long = timestamp.getTime()
    implicit def long2timestamp(t: Long): Timestamp = new Timestamp(t)
}


object DataJsonProtocol extends DefaultJsonProtocol {
    import Data._
    import metadata._


    implicit object TimestampJsonFormat extends JsonFormat[Timestamp] {
        def write(t:Timestamp) = JsNumber(t.getTime)

        def read(v: JsValue) = v match {
            case JsNumber(n) => new Timestamp(n.toLong)
            case JsString(s) => getTimestamp(s).getOrElse(deserializationError("Timestamp expected"))
            case _ => deserializationError("Timestamp expected")
        }
    }

    private def getTimestamp(s: String) : Option[Timestamp] = {
        val formats = List("dd.MM.yyyy HH:mm:ss:SSS", "dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd.MM.yyyy")

        formats.map{
            (formatString: String) => {
                Try(new Timestamp(new SimpleDateFormat(formatString).parse(s).getTime)) match {
                    case Success(t) => Some(t)
                    case Failure(_) => None
                }
            }
        }.dropWhile { ! _.isDefined }.headOption.getOrElse(None)
    }

    implicit val timeframeJsonFormat = jsonFormat(Timeframe.apply, "start", "end")

    implicit val borderJsonFormat = jsonFormat(OptBorder.apply, "top", "bottom", "left", "right")

    implicit val titleJsonFormat = jsonFormat(OptTitleParam.apply, "title", "position", "verticalOffset", "horizontalOffset", "size", "fontFamily", "alignment")

    implicit val legendJsonFormat = jsonFormat(OptLegendParam.apply, "position", "verticalOffset", "horizontalOffset", "symbolWidth", "size", "fontFamily")

    implicit val timeaxisJsonFormat = jsonFormat(OptTimeAxisParam.apply,
        "label", "labelSize", "labelFontFamily",
        "min", "max",
        "minDistance", "unit",
        "unitLabelSize", "unitLabelFontFamily",
        "showGrid", "showLabels",
        "arrowSize", "arrowFilled")

    implicit val valueaxisJsonFormat = jsonFormat(OptValueAxisParam.apply,
        "label", "labelSize", "labelFontFamily",
        "min", "max",
        "minDistance", "unit",
        "unitLabelSize", "unitLabelFontFamily",
        "showGrid", "showLabels",
        "arrowSize", "arrowFilled")

    implicit object ReliabilityJsonFormat extends JsonFormat[Reliability.Value] {
        def write(r: Reliability.Value) = JsString(r.toString)

        def read(v: JsValue): Reliability.Value = v match {
            case JsString(name) => try {
                Reliability.withName(name)
            } catch {
                case e:NoSuchElementException => deserializationError(s"Unknown value $name.")
            }
            case JsNumber(n) => try {
                Reliability(n.toInt)
            } catch {
                case e:NoSuchElementException => deserializationError(s"Unknown id $n.")
            }
            case _ => deserializationError("Invalid format for Reliability.")
        }
    }

    implicit object ResolutionJsonFormat extends JsonFormat[Resolution.Value] {
        def write(r: Resolution.Value) = JsString(r.toString)

        def read(v: JsValue): Resolution.Value = v match {
            case JsString(name) => try {
                Resolution.withName(name)
            } catch {
                case e:NoSuchElementException => deserializationError(s"Unknown value $name.")
            }
            case JsNumber(n) => try {
                Resolution(n.toInt)
            } catch {
                case e:NoSuchElementException => deserializationError(s"Unknown id $n.")
            }
            case _ => deserializationError("Invalid format for Resolution.")
        }
    }

    implicit object ScalingJsonFormat extends JsonFormat[Scaling.Value] {
        def write(r: Scaling.Value) = JsString(r.toString)

        def read(v: JsValue): Scaling.Value = v match {
            case JsString(name) => try {
                Scaling.withName(name)
            } catch {
                case e:NoSuchElementException => deserializationError(s"Unknown value $name.")
            }
            case JsNumber(n) => try {
                Scaling(n.toInt)
            } catch {
                case e:NoSuchElementException => deserializationError(s"Unknown id $n.")
            }
            case _ => deserializationError("Invalid format for Scaling.")
        }
    }

    implicit val activityTypeJsonFormat = jsonFormat(ActivityType.apply(_), "name")

    implicit val doubleValueFormat = jsonFormat(DoubleValue, "value")

    implicit val intValueFormat = jsonFormat(IntValue, "value")

    implicit object DataPointJsonFormat extends JsonFormat[DataPoint] {
        def write(d: DataPoint) = JsObject(Map[String, JsValue](
            "timestamp" -> d.timestamp.toJson,
            "value" -> d.value.value.toJson))

        def read(v: JsValue) = v match {
            case JsObject(fields) =>
                val valueType = fields.get("type").map(_.convertTo[String]).getOrElse("double")
                valueType.toLowerCase match {
                    case "int" => DataPoint(
                        fields.getOrElse("timestamp", deserializationError("missing key timestamp")).convertTo[Timestamp],
                        fields.getOrElse("value", deserializationError("missing key value")).convertTo[IntValue])
                    case "double" => DataPoint(
                        fields.getOrElse("timestamp", deserializationError("missing key timestamp")).convertTo[Timestamp],
                        fields.getOrElse("value", deserializationError("missing key value")).convertTo[DoubleValue])
                }
            case _ => deserializationError("Invalid format for DataPoint.")
        }
    }

    implicit val incompleteInputMetadataFormat = jsonFormat(IncompleteInputMetadata.apply,
        "timeframe", "reliability", "resolution", "scaling", "activityType", "size")

    implicit val completeInputMetadataFormat = jsonFormat(CompleteInputMetadata.apply,
        "timeframe", "reliability", "resolution", "scaling", "activityType", "size")

    implicit val unsafeInputDataJsonFormat = jsonFormat(UnsafeInputData.apply,
        "id", "measurement", "dataPoints", "metadata")

    implicit val inputDataJsonFormat = jsonFormat(InputData.apply,
        "id", "measurement", "dataPoints", "metadata")

    implicit val inputDataSubsetJsonFormat = jsonFormat(InputDataSubset.apply,
        "id", "measurement", "dataPoints", "metadata", "from", "to")
}
