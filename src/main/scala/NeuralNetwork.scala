package threesixty.machinelearning.perceptron


case class Neuron(val weights: Double*) {
    // def error: List[Double] = (for {_ <- 1 to weights.size} yield 0d).toList

    def actviation(n: Double): Double = n

    def apply(inputs: Double*): Double = actviation(inputs.zip(weights).map({ case (i, w) => i * w }).sum)

    override def toString(): String = weights.toString
}

object Neuron {
    def apply(n: Int): Neuron = Neuron((for {_ <- 1 to n} yield 1d).toSeq: _*)
}


class Layer(val neurons: Neuron*) {
    def apply(inputs: Double*): Seq[Double] = neurons.map({ n => n(inputs: _*) }).toList

    override def toString(): String = neurons.foldLeft("") { case (s, n) => s + "\t" + n.toString }
}

/*
object Layer {
    def apply(n: Int, k: Int): Layer = Layer((for {_ <- 1 to n} yield new Neuron(k)).toSeq: _*)
} */

/*
class Network(val layers: Layer*) {
    def train(input: List[Double], output: List[Double]): Unit = {
        val result = apply(input)

        val delta = result.zip(output).map({ case (r, o) => r == o })

        layers.foldRight(delta)({ case (layer, error) => ??? })
    }

    def apply(input: List[Double]): List[Double] = layers.foldLeft(input)({ case (in, layer) => layer(in) })

    override def toString(): String = layers.foldLeft("") { case (s, l) => s + l.toString + "\n" }
}
*/

case class SLPerceptron(val neuron: Neuron, val alpha: Double = 0.1, val threshold: Double = 1d) {
    def this(numberOfInputs: Int) = this(Neuron(numberOfInputs))

    // TODO
    def train(output: Boolean, input: Boolean*): SLPerceptron = {
        require(neuron.weights.size <= input.size)

        val result = apply(input: _*)

        if (result && !output) {
            SLPerceptron(Neuron((neuron.weights.zip(input).map {
                case (w: Double, false) => w
                case (w: Double, true) => w - alpha * 1.0
            }): _*), alpha, threshold)
        } else if (!result && output) {
            SLPerceptron(Neuron((neuron.weights.zip(input).map {
                case (w: Double, false) => w
                case (w: Double, true) => w + alpha * 1.0
            }): _*), alpha, threshold)
        } else {
            this
        }
    }

    def run(input: Boolean*): Double = neuron(input.map { case true => 1d; case false => 0d }: _*)

    def apply(input: Boolean*): Boolean = threshold < run(input: _*)
}



import threesixty.data.{InputData, DataPoint}
import threesixty.data.Data.{DoubleValue, Timestamp}
import threesixty.data.metadata._
import Resolution._
import Reliability._
import Scaling._

object EncodableInputData {
    implicit def pimp(data: InputData) = EncodableInputData(data)
    implicit def unpimp: EncodableInputData => InputData = { case EncodableInputData(data) => data }
}

case class EncodableInputData(data: InputData) {
    def boolEncode: Seq[Boolean] = Seq(
        data.metadata.reliability == Device,
        data.metadata.reliability == User,
        data.metadata.reliability == Unknown,
        data.metadata.resolution  == High,
        data.metadata.resolution  == Low,
        data.metadata.resolution  == Middle,
        data.metadata.scaling     == Nominal,
        data.metadata.scaling     == Ordinal,
        data.metadata.timeframe.length > 3600000L,
        data.metadata.timeframe.length > 86400000L,
        data.metadata.timeframe.length > 604800000L,
        data.metadata.timeframe.length > 2419200000L
    )
}



object PerceptronTest extends App {

    var perceptron = SLPerceptron(Neuron(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))

    for { _ <- 1 to 1000 } {
        for { start       <- List(10L, 1000L, 10000L, 100000L, 1000000L, 10000000L);
              end         <- List(50L, 5000L, 50000L, 500000L, 5000000L, 50000000L);
              if start < end;
              reliability <- List(Device, User, Unknown);
              resolution  <- List(High, Middle, Low);
              scaling     <- List(Nominal, Ordinal) } {
            val metadata = CompleteInputMetadata(
                Timeframe(new Timestamp(start), new Timestamp(end)),
                reliability,
                resolution,
                scaling,
                ActivityType(".")
            )
            val inputData = InputData(".", ".", List(DataPoint(new Timestamp(0), DoubleValue(1.0))), metadata)
            perceptron = perceptron.train(scala.util.Random.nextBoolean, (inputData: EncodableInputData).boolEncode: _*)
        }
    }

    println(perceptron)

    val test: EncodableInputData = InputData(".", ".", List(DataPoint(new Timestamp(0), DoubleValue(1.0))), CompleteInputMetadata(
        Timeframe(new Timestamp(0), new Timestamp(10000)), User, Middle, Nominal, ActivityType(".")))

    println(perceptron(test.boolEncode: _*))

}
