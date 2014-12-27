package formats

import models._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONObjectID

trait APIJsonFormats extends CommonJsonFormats {
  implicit val objectIDWrite: Writes[BSONObjectID] = Writes{ (objectID: BSONObjectID) => JsString(objectID.stringify) }
  implicit val objectIDRead: Reads[BSONObjectID] = (JsPath \ "$oid").read[String].map(BSONObjectID(_))

  def mongoReads[T](r: Reads[T]) = {
    __.json.update((__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString] )) andThen r
  }

  def mongoWrites[T](w : Writes[T]) = {
    w.transform( js => js.as[JsObject] - "_id"  ++ Json.obj("id" -> (js \ "_id")) )
  }

  implicit val tokenWrites: Writes[Token] = mongoWrites[Token](Json.writes[Token])

  implicit val userWrite: Writes[User] = (
    (__ \ "email").write[String] and
    (__ \ "id").write[BSONObjectID] and
    (__ \ "state").writeNullable[String]
  )(user => (user.email, user._id, user.state))

  private val sha256Regex = "[0-9a-z]{64}".r
  private val emailRegex = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  implicit val newUserRead: Reads[NewUser]  = (
    (__ \ "email").read[String](pattern(emailRegex, "Invalid email address")) and
    (__ \ "password").read[String](pattern(sha256Regex, "Password should be sha256(email:password)"))
  )(NewUser.apply _)

  implicit val errorWrite = Json.writes[Error]
  implicit val emailWrite = Json.writes[Email]
  implicit val topLevelWrite = Json.writes[TopLevel]
}
