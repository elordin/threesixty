package threesixty.persistence.cassandra.tables

import java.util.UUID

import com.websudos.phantom.dsl._
import threesixty.data.metadata.{ActivityType}

import scala.concurrent.Future

/**
  * Created by Stefan Cimander on 14.01.16.
  */
class ActivityTypesTable extends CassandraTable[ActivityTypes, ActivityType] {

    object identifier extends UUIDColumn(this) with PartitionKey[UUID]
    object name extends StringColumn(this)
    object description extends StringColumn(this)

    def fromRow(row: Row): ActivityType = {
        val resultName = name(row)
        val resultDescription = description(row)

        val resultActivityType = ActivityType(name(row))
        if (resultDescription.nonEmpty) {
            resultActivityType.setDescription(resultDescription)
        }
        resultActivityType
    }
}


abstract class ActivityTypes extends ActivityTypesTable with RootConnector {

    def store(activity: ActivityType, identifier: UUID = UUID.randomUUID()): Future[ResultSet] = {
        val descriptionText = Option(activity.description) match {
            case Some(description) => description
            case None => ""
        }

        insert.value(_.identifier, identifier)
            .value(_.name, activity.name)
            .value(_.description, descriptionText)
            .consistencyLevel_=(ConsistencyLevel.ALL)
            .future()
    }

    def getById(identifier: UUID): Future[Option[ActivityType]] = {
        select.where(_.identifier eqs identifier).one()
    }

}
