case class Neuron(val weights: Double*) {

    def this(n: Int) = this((for {_ <- 1 to n} yield 1d).toSeq: _*)

    // def error: List[Double] = (for {_ <- 1 to weights.size} yield 0d).toList

    def actviation(n: Double): Double = if (n > 0d) 1d else 0d

    def apply(inputs: List[Double]): Double = actviation(inputs.zip(weights).map({ case (i, w) => i * w }).sum)

    override def toString(): String = weights.toString

}


class Layer(val neurons: Neuron*) {

    def this(n: Int, k: Int) = this((for {_ <- 1 to n} yield new Neuron(k)).toSeq: _*)

    def apply(inputs: List[Double]): List[Double] = neurons.map({ n => n(inputs) }).toList

    override def toString(): String = neurons.foldLeft("") { case (s, n) => s + "\t" + n.toString }
}


class Network(val layers: Layer*) {
    def train(input: List[Double], output: List[Double]): Unit = {
        val result = apply(input)

        val delta = result.zip(output).map({ case (r, o) => r == o })

        layers.foldRight(delta)({ case (layer, error) => ??? })
    }

    def apply(input: List[Double]): List[Double] = layers.foldLeft(input)({ case (in, layer) => layer(in) })

    override def toString(): String = layers.foldLeft("") { case (s, l) => s + l.toString + "\n" }

}


case class SLPerceptron(val neuron: Neuron) {
    def this(numberOfInputs: Int) = this(Neuron(numberOfInputs))

    // TODO
    def train(input: List[Boolean], output: Boolean): SLPerceptron = {
        if (apply(input) == output) {
            SLPerceptron(Neuron((neuron.weights.zip(input).map {
                case (w, true)  => w * 1.1
                case (w, false) => w * 0.9
            }): _*))
        } else {
            SLPerceptron(Neuron((neuron.weights.zip(input).map {
                case (w, false) => w * 1.1
                case (w, true)  => w * 0.9
            }): _*))
        }
    }

    def run(input: List[Boolean]): Double = neuron(input.map { case true => 1d; case false => 0d })

    def apply(input: List[Boolean]): Boolean = 1d < run(input)

}

