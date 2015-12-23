package threesixty.processor

import threesixty.config.Config
import threesixty.data.ProcessedData

sealed trait ProcessingMethod

trait SingleProcessingMethod extends ProcessingMethod with Function1[ProcessedData, ProcessedData]
trait MultiProcessingMethod  extends ProcessingMethod with Function1[Set[ProcessedData], Set[ProcessedData]]


case class ProcessingStrategy(methods: ProcessingMethod*)
        extends Function2[Set[ProcessedData], Config, Set[ProcessedData]] {

    /**
     *  Processes the input dataset based on the provided methods and config.
     *  @param data Datasets to be prosessed
     *  @param config Configuration constraining the processing CURRENTLY NOT USED
     *  @returns Processed dataset
     */
    def apply(data:Set[ProcessedData], config:Config):Set[ProcessedData] =
        methods.foldLeft(data) {
            case (d:Set[ProcessedData], m:MultiProcessingMethod) =>
                m(d)
            case (d:Set[ProcessedData], m:SingleProcessingMethod) =>
                d map m
        }

    /**
     *  Alternative way of calling the processing strategy.
     *  @see apply
     */
    // def process = apply(_,_)
    def process(data:Set[ProcessedData], config:Config) = apply(data, config)

}

