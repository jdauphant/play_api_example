package models


import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import scala.concurrent.ExecutionContext.Implicits.global

case class User(email: String, password: Option[String]= None, _id: Option[BSONObjectID] = None, state: Option[String] = None)

object User extends MongoModel {
  def index = collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true))

  def collection: JSONCollection = db.collection[JSONCollection]("users")

  implicit val userFormat = Json.format[User]

  def create(user: User) = collection.insert(user)

  def findByEmail(email: String) = collection.find(Json.obj("email" -> email)).cursor[User].collect[List]()
}