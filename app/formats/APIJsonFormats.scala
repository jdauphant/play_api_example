package formats

import models._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONObjectID

trait APIJsonFormats extends CommonJsonFormats {
  implicit val tokenWrites: Writes[Token] = Json.writes[Token]

  implicit val userWrite: Writes[User] = Json.writes[User].transform( js => js.as[JsObject] - "passwordHash" )

  private val sha256Regex = "[0-9a-z]{64}".r
  private val emailRegex = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  private val usernameRegex = "[0-9a-zA-Z.]{2,20}".r
  implicit val newUserRead: Reads[NewUser]  = (
    (__ \ "email").read[String](pattern(emailRegex, "error.email")) and
    (__ \ "password").read[String](pattern(sha256Regex, "error.sha256")) and
    (__ \ "username").read[String](pattern(usernameRegex, "error.username"))
  )(NewUser.apply _)

  implicit val loginUserRead: Reads[LoginUser]  = (
    (__ \ "email").readNullable[String](pattern(emailRegex, "error.email")) and
    (__ \ "password").read[String](pattern(sha256Regex, "error.sha256")) and
    (__ \ "username").readNullable[String](pattern(usernameRegex, "error.username"))
  )(LoginUser.apply _)

  implicit val errorWrite = Json.writes[Error]
  implicit val emailWrite = Json.writes[Email]
  implicit val topLevelWrite = Json.writes[TopLevel]

}
