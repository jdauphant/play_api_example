package models

import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global

case class User(email: String, passwordHash: String, _id: Option[BSONObjectID] = None, state: Option[String] = None)

object User extends MongoModel("users") {
  def ensureIndexes = collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true))

  def create(user: User) = collection.insert(user)

  def findByEmail(email: String) = collection.find(Json.obj("email" -> email)).cursor[User].collect[List]()
}