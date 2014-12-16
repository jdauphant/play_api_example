package models

import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global

case class User(email: String, password: Option[String]= None, _id: Option[BSONObjectID] = None, state: Option[String] = None)

object User extends MongoModel {
  def ensureIndexes = collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true))

  def collection: JSONCollection = db.collection[JSONCollection]("users")
  def create(user: NewUser) = collection.insert(user)

  def findByEmail(email: String) = collection.find(Json.obj("email" -> email)).cursor[User].collect[List]()
}