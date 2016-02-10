package threesixty.processor

import threesixty.data.{DataPool}


/**
 *  Aggregation of multiple [[threesixty.processor.ProcessingStep]]s.
 *
 *  Acts as a function that is applied to a pool of data, processing
 *  the ProcessedData stored in that pool by piping it through
 *  the ProcessingSteps. It updates the processed data in the given pool.
 *
 *  @author Thomas Weber
 *
 *  @param steps Individual processing steps in the pipeline
 */
case class ProcessingStrategy(steps: ProcessingStep*)
        extends Function1[DataPool, Unit] {

    /**
     *  Processes the input dataset based on the provided methods and config.
     *  @param pool Data pool at the current state in processing pipeline.
     */
    def apply(pool: DataPool): Unit =
        steps.foreach {
            step => pool.pushData(step.run(pool))
        }

    /**
     *  Alternative way of calling the processing strategy.
     *  @see apply
     */
    def process(pool: DataPool): Unit = apply(pool)

}
