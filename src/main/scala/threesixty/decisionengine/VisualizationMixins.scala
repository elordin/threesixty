package threesixty.decisionengine.visualizations

import threesixty.decisionengine.machinelearning.{SLPerceptron, Neuron}

import threesixty.processor.ProcessingStrategy
import threesixty.data.InputDataSkeleton
import threesixty.data.metadata._
import Resolution._
import Reliability._
import Scaling._

import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait BoolEncodable {
    def encode: Seq[Boolean]
}

trait Trainable[E <: BoolEncodable] {
    def train(chooseThis: Boolean, inputs: E*): Unit
}


trait VisualizationDecisionMethod {

    def numberOfDeciders = 0

    def degreeOfFit(skeletons: InputDataSkeleton*): Double
    def degreeOfFit(processingStrategy: ProcessingStrategy, skeletons: InputDataSkeleton*): Double
}


object EncodableInputDataSkeleton {
    def ENCODED_LENGTH = 12
    implicit def fromInputDataSkeleton(s: InputDataSkeleton) = EncodableInputDataSkeleton(s)
    implicit def toInputDataSkeleton(e: EncodableInputDataSkeleton) = e.skeleton
}

case class EncodableInputDataSkeleton(skeleton: InputDataSkeleton) extends BoolEncodable {
    def encode: Seq[Boolean] = Seq(
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
}

trait PerceptronVizMixin extends VisualizationDecisionMethod with Trainable[EncodableInputDataSkeleton] {

    def train(chooseThis: Boolean, skeletons: EncodableInputDataSkeleton*): Unit = {
        val trainingsFuture =
            skeletons.foldLeft(Future[SLPerceptron] { this.perceptron } ) {
                (future: Future[SLPerceptron], skeleton: EncodableInputDataSkeleton) =>
                    future.andThen {
                        case Success(newPerceptron: SLPerceptron) =>
                            newPerceptron.train(chooseThis, (skeleton: EncodableInputDataSkeleton).encode: _*)
                        case Failure(_) => this.perceptron
                    }
            }

        trainingsFuture onSuccess {
            case newPerceptron: SLPerceptron => this.perceptron = newPerceptron
        }
    }

    var perceptron = SLPerceptron(Neuron((for { _ <- 1 to EncodableInputDataSkeleton.ENCODED_LENGTH } yield 0.5): _*))

    abstract override def degreeOfFit(skeletons: InputDataSkeleton*): Double =
        skeletons.map({
            skeleton: InputDataSkeleton => perceptron.run((skeleton: EncodableInputDataSkeleton).encode: _*)
        }).sum / skeletons.size / numberOfDeciders + super.degreeOfFit(skeletons: _*)

    abstract override def numberOfDeciders = super.numberOfDeciders + 1

}
