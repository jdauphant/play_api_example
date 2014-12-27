package models

import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global

case class User(email: String, passwordHash: String, _id: BSONObjectID, state: Option[String] = None)

object User extends MongoModel("users") {
  def ensureIndexes = collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true))

  def findByEmail(email: String) = collection.find(Json.obj("email" -> email)).cursor[User].collect[List]()
  def fromNewUser(newUser: NewUser): User = new User(newUser.email,Hash.bcrypt(newUser.password),BSONObjectID.generate)

  def create(user: User) = collection.insert(user)

}