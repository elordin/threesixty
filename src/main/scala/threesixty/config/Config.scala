package threesixty.config

import threesixty.data.{InputData, ProcessedData}
import threesixty.data.Data.Identifier
import threesixty.data.Implicits.input2ProcessedData

import scala.collection.immutable.{Map => ImmutableMap}

object Implicits {
    import spray.json._

    /**
     *  Implicit conversion to parse a JSON Object to Config
     */
    // since Config is a case class, easier conversions might be available.
    // Check the spray-json docs at https://github.com/spray/spray-json
    implicit object ConfigJsonFormat extends RootJsonFormat[Config] {

        def write(c: Config) = throw new NotImplementedError

        def read(value: JsValue) = throw new NotImplementedError

    }
}

/**
 *  Config potentially contains all options transmitted from the client.
 *  Some values may be required, some may be optional.
 *
 *  @param dataIDs Set of IDs of datasets that will be processed.
 */
@throws[NoSuchElementException]("if an id was given, for which no InputData exists.")
class Config(val dataIDs: Set[Identifier]) {

    require(dataIDs.size > 0, "Empty ID Set is not allowed.")

    // get data from db
    val inputDatasets: Set[InputData] = Set.empty // dataIDs.map(getFromDB)

    // convert input data to processed data
    var processedDatasets: Map[Identifier, ProcessedData] =
        (for { data <- inputDatasets} yield (data.id, input2ProcessedData(data))).toMap

    /**
     *  Inserts data into the processedDatasets Map
     *  @param data Set of data to be added to processedDatasets
     */
    def pushData(data: Set[ProcessedData]): Unit = {
        data.foreach {
            d => processedDatasets += (d.id -> d)
        }
    }

    /**
     *  Accessor for processedDatasets
     *  @return Immutable Map of Identifier -> ProcessedData
     */
    def datasets: ImmutableMap[Identifier, ProcessedData] = processedDatasets

}

