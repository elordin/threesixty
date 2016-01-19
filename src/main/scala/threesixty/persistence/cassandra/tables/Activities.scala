package threesixty.persistence.cassandra.tables

import java.util.UUID

import com.websudos.phantom.dsl._
import threesixty.data.metadata.{ActivityType}

import scala.concurrent.Future

/**
  * Created by Stefan Cimander on 14.01.16.
  */
class ActivitiesTable extends CassandraTable[Activities, ActivityType] {

    // object id extends UUIDColumn(this) with PartitionKey[UUID]
    object name extends StringColumn(this)
    object description extends StringColumn(this)

    def fromRow(row: Row): ActivityType = {
        val activityType = ActivityType(name(row))
        activityType.setDescription(description(row))
        activityType
    }
}


abstract class Activities extends ActivitiesTable with RootConnector {

    def store(activity: ActivityType): Future[ResultSet] = {
        insert.value(_.name, activity.name)
            .value(_.description, activity.description)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }
    /*
    def getById(id: UUID): Future[Option[ActivityType]] = {
        select.where(_.id eqs id).one()
    }
    */
}
