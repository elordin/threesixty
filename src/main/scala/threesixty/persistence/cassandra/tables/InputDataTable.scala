package threesixty.persistence.cassandra.tables

import java.sql.Timestamp
import java.util.UUID

import com.websudos.phantom.dsl._
import threesixty.data.InputData
import threesixty.data.metadata._

import scala.concurrent.Future

/**
  * Created by Stefan Cimander on 19.01.16.
  */
class InputDataTable extends CassandraTable[InputDatasets, InputData] {

    object identifier extends UUIDColumn(this) with PartitionKey[UUID]
    object measurement extends StringColumn(this)

    def fromRow(row: Row): InputData = {
        val resultIdentifier = identifier(row).toString
        val resultMeasurement = measurement(row)

        // TODO: Load input metadata instead of using hardcoded values
        val startTime = new Timestamp(0L)
        val endTime = new Timestamp(1L)
        val timeframe = new Timeframe(startTime, endTime)

        val reliability = Reliability.User
        val resolution = Resolution.Low
        val scaling = Scaling.Nominal

        val activityType = new ActivityType("Some activity")
        val metadata = CompleteInputMetadata(timeframe, reliability, resolution, scaling, activityType)


        InputData(resultIdentifier, resultMeasurement, List(), metadata)
    }
}

abstract class InputDatasets extends InputDataTable with RootConnector {

    def getInputDataByIdentifier(identifier: UUID): Future[Option[InputData]] = {
        select.where(_.identifier eqs identifier).one()
    }

}
