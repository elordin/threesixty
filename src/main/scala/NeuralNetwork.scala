class Neuron(val weights: Double*) {

    def this(n: Int) = this((for {_ <- 1 to n} yield 0d).toSeq: _*)

    def error: List[Double] = (for {_ <- 1 to weights.size} yield 0d).toList

    def actviation(n: Double): Double = if (n > 0d) 1d else 0d

    def apply(inputs: List[Double]): Double = actviation(inputs.zip(weights).map({ case (i, w) => i * w }).sum)

    override def toString(): String = "[ ]"

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
