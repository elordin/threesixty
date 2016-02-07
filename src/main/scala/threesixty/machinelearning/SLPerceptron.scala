package threesixty.machinelearning


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


import threesixty.visualizer.VisualizationCompanion
import threesixty.data.InputDataSkeleton
import threesixty.data.metadata._
import Resolution._
import Reliability._
import Scaling._

import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait PerceptronVizMixin extends VisualizationCompanion {

    final val ENCODING_LENGTH = 12
    final def encodeMetadata(skeleton: InputDataSkeleton): Seq[Boolean] = Seq(
        skeleton.metadata.reliability == Device,
        skeleton.metadata.reliability == User,
        skeleton.metadata.reliability == Unknown,
        skeleton.metadata.resolution  == High,
        skeleton.metadata.resolution  == Low,
        skeleton.metadata.resolution  == Middle,
        skeleton.metadata.scaling     == Nominal,
        skeleton.metadata.scaling     == Ordinal,
        skeleton.metadata.timeframe.length > 3600000L,
        skeleton.metadata.timeframe.length > 86400000L,
        skeleton.metadata.timeframe.length > 604800000L,
        skeleton.metadata.timeframe.length > 2419200000L
    )

    def train(chooseThis: Boolean, skeletons: InputDataSkeleton*): Unit = {
        val trainingsFuture =
            skeletons.foldLeft(Future[SLPerceptron] { this.perceptron } ) {
                (future: Future[SLPerceptron], skeletons: InputDataSkeleton) =>
                    future.andThen {
                        case Success(newPerceptron: SLPerceptron) =>
                            newPerceptron.train(chooseThis, encodeMetadata(skeletons): _*)
                        case Failure(_) => this.perceptron
                    }
            }

        trainingsFuture onSuccess {
            case newPerceptron: SLPerceptron => this.perceptron = newPerceptron
        }
    }

    var perceptron = SLPerceptron(Neuron((for { _ <- 1 to ENCODING_LENGTH } yield 0.5): _*))

    abstract override def degreeOfFit(skeletons: InputDataSkeleton*): Double =
        (super.degreeOfFit(skeletons: _*) + skeletons.map({
            imd: InputDataSkeleton => perceptron.run(encodeMetadata(imd): _*)
        }).sum / skeletons.size) / 2

}
