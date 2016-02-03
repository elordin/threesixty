package threesixty.decisionengine.visualizations

import threesixty.decisionengine.machinelearning.{SLPerceptron, Neuron}

import threesixty.visualizer.VisualizationCompanion
import threesixty.data.metadata._
import Resolution._
import Reliability._
import Scaling._

import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait VisualizationDecider {

    def numberOfDeciders = 0

    def degreeOfFit(inputMetadata: CompleteInputMetadata*): Double = 0
    def degreeOfFit(processingStrategy: ProcessingStrategy, inputMetadata: CompleteInputMetadata*): Double = 0

}


trait PerceptronVizMixin extends VisualizationDecider {

    final val ENCODING_LENGTH = 12
    final def encodeMetadata(metadata: CompleteInputMetadata): Seq[Boolean] = Seq(
        metadata.reliability == Device,
        metadata.reliability == User,
        metadata.reliability == Unknown,
        metadata.resolution  == High,
        metadata.resolution  == Low,
        metadata.resolution  == Middle,
        metadata.scaling     == Nominal,
        metadata.scaling     == Ordinal,
        metadata.timeframe.length > 3600000L,
        metadata.timeframe.length > 86400000L,
        metadata.timeframe.length > 604800000L,
        metadata.timeframe.length > 2419200000L
    )

    def train(chooseThis: Boolean, datasets: CompleteInputMetadata*): Unit = {
        val trainingsFuture =
            datasets.foldLeft(Future[SLPerceptron] { this.perceptron } ) {
                (future: Future[SLPerceptron], metadata: CompleteInputMetadata) =>
                    future.andThen {
                        case Success(newPerceptron: SLPerceptron) =>
                            newPerceptron.train(chooseThis, encodeMetadata(metadata): _*)
                        case Failure(_) => this.perceptron
                    }
            }

        trainingsFuture onSuccess {
            case newPerceptron: SLPerceptron => this.perceptron = newPerceptron
        }
    }

    var perceptron = SLPerceptron(Neuron((for { _ <- 1 to ENCODING_LENGTH } yield 0.5): _*))

    abstract override def degreeOfFit(inputMetadata: CompleteInputMetadata*): Double =
        inputMetadata.map({
            imd: CompleteInputMetadata => perceptron.run(encodeMetadata(imd): _*)
        }).sum / inputMetadata.size / numberOfDeciders + super.degreeOfFit(inputMetadata: _*)

    abstract override def numberOfDeciders = super.numberOfDeciders + 1

}


trait HeuristicVizMixin extends VisualizationDecider {

}
