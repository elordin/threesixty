package threesixty.decisionengine.machinelearning


case class Neuron(val weights: Double*) {
    def actviation(n: Double): Double = n

    def apply(inputs: Double*): Double = actviation(inputs.zip(weights).map({ case (i, w) => i * w }).sum)

    override def toString(): String = weights.toString
}

object Neuron {
    def apply(n: Int): Neuron = Neuron((for {_ <- 1 to n} yield 1d).toSeq: _*)
}

case class SLPerceptron(val neuron: Neuron, val alpha: Double = 0.1, val threshold: Double = 1d) {
    def this(numberOfInputs: Int) = this(Neuron(numberOfInputs))

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
