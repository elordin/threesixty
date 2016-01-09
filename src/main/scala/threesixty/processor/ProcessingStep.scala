package threesixty.processor

import threesixty.data.ProcessedData

import scala.collection.parallel._


/**
 *  Represents single step in the processing chain.
 *  Works on a subset of all data, namely all that data that is being subjected
 *  to this particular processing method.
 *
 *  Operations that run on each individual dataset without affecting the others
 *  are run in parallel.
 *
 *  @param method Method of processing for this step
 *  @param Subset of all data that is to be processed in this step.
 */
case class ProcessingStep(val method:ProcessingMethod, val data:Set[ProcessedData]) {
    def run:Set[ProcessedData] = {
        method match {
            case m:SingleProcessingMethod => data.par.map(m(_)).seq
            case m:MultiProcessingMethod => m(data)
        }
    }
}
