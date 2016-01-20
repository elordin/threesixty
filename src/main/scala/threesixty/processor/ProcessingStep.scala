package threesixty.processor

import threesixty.data.ProcessedData
import threesixty.data.Data.Identifier
import threesixty.config.Config

import scala.collection.parallel._


sealed abstract class ProcessingMethod(idMapping: Map[Identifier, Identifier])


/**
 *  ProcessingMethod that works only on one single dataset.
 *  It may however create datasets, and thus returns a Set of ProcessedData.
 *
 *  @author Thomas Weber
 *
 *  @param Single instance of ProcessedData it requires
 *  @return Set of ProcessedData, possibly including artificially created data
 */
abstract class SingleProcessingMethod(idMapping: Map[Identifier, Identifier])
    extends ProcessingMethod(idMapping: Map[Identifier, Identifier])
    with Function1[ProcessedData, Set[ProcessedData]]


/**
 *  ProcessingMethod that requires multiple datasets to process.
 *
 *  @author Thomas Weber
 *
 *  @param Set of ProcessedData that is going to process
 *  @return Set of ProcessedData, possibly including artificially created data
 */
abstract class MultiProcessingMethod(idMapping: Map[Identifier, Identifier])
    extends ProcessingMethod(idMapping: Map[Identifier, Identifier])
    with Function1[Set[ProcessedData], Set[ProcessedData]]


/**
 *  Represents single step in the processing chain.
 *  Works on a subset of all data, namely all that data that is being subjected
 *  to this particular processing method.
 *
 *  Operations that run on each individual dataset without affecting the others
 *  are run in parallel.
 *
 *  @author Thomas Weber
 *
 *  @param method Method of processing for this step
 *  @param IDs of a subset of all data that is to be processed in this step.
 */
case class ProcessingStep(val method:ProcessingMethod, val dataIDs:Set[Identifier]) {

    @throws[NoSuchElementException]("if ProcessedData for one of the ids could not be found")
    def run(datasetPool: Map[Identifier, ProcessedData]): Set[ProcessedData] = {
        method match {
            case m:SingleProcessingMethod => dataIDs.map(datasetPool(_)).par.flatMap(m(_)).seq
            case m:MultiProcessingMethod => m(dataIDs.map(datasetPool(_)))
        }
    }
}
