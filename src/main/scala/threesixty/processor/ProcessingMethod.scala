package threesixty.processor

import threesixty.config._

trait ProcessedData

trait ProcessingMethod

trait SingleProcessingMethod extends Function1[ProcessedData, Int]      with ProcessingMethod
trait MultiProcessingMethod  extends Function1[Set[ProcessedData], Int] with ProcessingMethod


case class ProcessingStrategy(methods: ProcessingMethod*)
        extends Function2[Set[ProcessedData], Config, Set[ProcessedData]] {

    /**
     *  Processes the input dataset based on the provided methods and config.
     *  @param data Datasets to be prosessed
     *  @param config Configuration restraining the processing
     *  @returns Processed dataset
     */
    def apply(data:Set[ProcessedData], config:Config):Set[ProcessedData] =
        throw new NotImplementedError

    /**
     *  Alternative way of calling the processing strategy.
     *  @see apply
     */
    // def process = apply(_,_)
    def process(data:Set[ProcessedData], config:Config) = apply(data, config)

}

