package models

import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global

case class User(email: String, passwordHash: Option[String], id: String, username: String, facebookToken: Option[String], facebookUserId: Option[String], state: Option[String] = None)
case class NewUser(email: String, password: Option[String], username: String, facebookToken: Option[String])
case class LoginUser(email: Option[String], password: Option[String], username: Option[String], facebookToken: Option[String])

object User extends MongoModel("users") {
  def ensureIndexes = {
    collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true, sparse = true))
    collection.indexesManager.ensure(Index(Seq("username" -> IndexType.Ascending), name = Some("usernameUniqueIndex"), unique = true, sparse = true))
    collection.indexesManager.ensure(Index(Seq("facebookUserId" -> IndexType.Ascending), name = Some("facebookUserIdUniqueIndex"), unique = true, sparse = true))
  }

  def fromNewUser(newUser: NewUser, facebookUserId: Option[String] = None): User = new User(
    newUser.email,
    newUser.password.map(Hash.bcrypt),
    BSONObjectID.generate.stringify,
    newUser.username,
    newUser.facebookToken,
    facebookUserId)

  def create(user: User) = collection.insert(user)
  def findById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[User].collect[List]()
  def findByEmail(email: String) = collection.find(Json.obj("email" -> email)).cursor[User].collect[List]()
  def findByUsername(username: String) = collection.find(Json.obj("username" -> username)).cursor[User].collect[List]()
  def findByFacebookUserId(facebookUserId: String) = collection.find(Json.obj("facebookUserId" -> facebookUserId)).cursor[User].collect[List]()

}