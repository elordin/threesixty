package threesixty.processor

import threesixty.data.{ProcessedData, DataPool}
import threesixty.data.Data.Identifier

import scala.collection.parallel._


/**
 * Represents single step in the processing chain.
 *
 * Works on a subset of all data, namely all that data that is being subjected
 * to this particular processing method.
 *
 * Operations that run on each individual dataset without affecting the others
 * are run in parallel using scala.collection.parallel.ParSet.
 *
 * @author Thomas Weber
 *
 * @param method Method of processing for this step
 * @param IDs of a subset of all data that is to be processed in this step.
 */
case class ProcessingStep(val method: ProcessingMethod, val dataIDs: Set[Identifier]) {

    @throws[NoSuchElementException]("if ProcessedData for one of the ids could not be found")
    def run(pool: DataPool): Set[ProcessedData] = {
        method match {
            case m: SingleProcessingMethod => dataIDs.map(pool(_)).par.flatMap(m(_)).seq
            case m: MultiProcessingMethod => m(dataIDs.map(pool(_)))
        }
    }
}
