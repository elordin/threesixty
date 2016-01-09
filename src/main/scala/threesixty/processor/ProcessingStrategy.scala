package threesixty.processor

import threesixty.config.Config
import threesixty.data.ProcessedData

/**
 *  Aggregation of multiple ProcessingSteps.
 *  Acts as a function that is applied to a configuration, processing
 *  the ProcessedData stored in that configuration by piping it through
 *  the ProcessingSteps. It updates the processed data in the given config.
 *
 *  @author Thomas Weber
 *
 *  @param config Configuration containing the data to be processed
 *  @return Unit. New and updated data is inserted into the config.
 */
case class ProcessingStrategy(steps: ProcessingStep*)
        extends Function1[Config, Unit] {

    /**
     *  Processes the input dataset based on the provided methods and config.
     *  @param config Configuration defining the current processing pipeline.
     */
    def apply(config:Config): Unit =
        steps.foreach {
            step =>
                val result = step.run(config.datasets)
                config.pushData(result)
        }

    /**
     *  Alternative way of calling the processing strategy.
     *  @see apply
     */
    // def process = apply(_)
    def process(config:Config): Unit = apply(config)

}

