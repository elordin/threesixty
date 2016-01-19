package threesixty.data

import threesixty.data.tags.{InputOrigin}
import java.sql.{Timestamp => JSQLTimestamp}

object Data {

    type Identifier = String
    type Timestamp  = JSQLTimestamp

    trait ValueType {
        def value: Double
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
    import Data.{Timestamp, Identifier, DoubleValue, IntValue}

    implicit def input2ProcessedData:(InputData) => ProcessedData = {
        case input@InputData(id: Identifier, _, data:List[DataPoint], metadata) =>
            ProcessedData(id, data.map {
                case DataPoint(timestamp, value) =>
                    TaggedDataPoint(timestamp, value, Set(InputOrigin(input)))
                })
    }

    implicit def unsafe2safeInputData(unsafe: UnsafeInputData)(implicit context: InputData): InputData =
        InputData(
            unsafe.id,
            unsafe.measurement,
            unsafe.data,
            unsafe.metadata.complete(context)
        )

    implicit def timestamp2Long(timestamp: Timestamp): Long = timestamp.getTime()
    implicit def long2timestamp(t: Long): Timestamp = new Timestamp(t)
}
