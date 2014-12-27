package models

import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global

case class User(email: String, passwordHash: String, id: String, state: Option[String] = None)

object User extends MongoModel("users") {
  def ensureIndexes = collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true))

  def findByEmail(email: String) = collection.find(Json.obj("email" -> email)).cursor[User].collect[List]()
  def fromNewUser(newUser: NewUser): User = new User(newUser.email,Hash.bcrypt(newUser.password),BSONObjectID.generate.stringify)

  def create(user: User) = collection.insert(user)
  def findById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[User].collect[List]()
}