package threesixty.data

object Data {


    trait ValueType {
        def value: Double
    }

    /*
    trait BoundedValueType extends ValueType {
        def upperBound: this.type / Double
        def lowerBound: this.type / Double
    }
    trait UnboundedValueType extends ValueType
    */

    case class IntValue(_value: Int) extends ValueType {
        def value: Double = _value
    }

    implicit def fromIntValue(iValue: IntValue): Double = iValue.value
    implicit def toIntValue(value: Int): IntValue = IntValue(value)


    case class DoubleValue(val value: Double) extends ValueType

    implicit def fromDoubleValue(dValue: DoubleValue): Double = dValue.value
    implicit def toDoubleValue(value: Double): DoubleValue = DoubleValue(value)


    abstract class DataEnum extends Enumeration {
        protected case class EnumValue(
            val i: Int,
            val name: String,
            val value: Double
        ) extends Val(i: Int, name: String) with ValueType {

        }

        implicit def fromEnumValue(eValue: EnumValue): Double = eValue.value
        implicit def toEnumValue(value: Double): EnumValue
        // throw new NoSuchElementException(s"No value found for '$value'"))
    }

}
