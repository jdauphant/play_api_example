package formats

import models._
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONObjectID

trait APIJsonFormats extends CommonJsonFormats {
  implicit def traversableWrites[A: Writes] = new Writes[Traversable[A]] {
    def writes(as: Traversable[A]) = JsArray(as.map(Json.toJson(_)).toSeq)
  }

  def addHref[T](objType: String, w : Writes[T]): Writes[T] = w.transform {
    js =>
      js.as[JsObject] ++
      Json.obj("href" -> JsString("/%s/%s".format(objType,(js \ "id").as[String])),
      "type" -> objType
      )
  }

  implicit val tokenWrites: Writes[Token] = addHref("tokens",Json.writes[Token].transform{
    js => js.as[JsObject] - "userId" ++
      Json.obj("user" ->
        Json.obj("id" ->  js \ "userId",
        "href" -> JsString("/users/" + (js \ "userId").as[String]),
        "type" -> "users"))
  })

  implicit val userWrite: Writes[User] = addHref("users",Json.writes[User].transform( js => js.as[JsObject] - "passwordHash" - "facebookToken"))

  private val sha256Regex = "[0-9a-z]{64}".r
  private val emailRegex = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  private val usernameRegex = "[0-9a-zA-Z.]{2,20}".r
  private val facebookTokenRegex = "[^;\t\n]{1,1024}".r
  implicit val newUserRead: Reads[NewUser]  = (
    (__ \ "email").read[String](pattern(emailRegex, "error.email")) and
    (__ \ "password").readNullable[String](pattern(sha256Regex, "error.sha256")) and
    (__ \ "username").read[String](pattern(usernameRegex, "error.username")) and
    (__ \ "facebookToken").readNullable[String](pattern(facebookTokenRegex, "error.facebookToken"))
    )(NewUser.apply _)

  implicit val loginUserRead: Reads[LoginUser]  = (
    (__ \ "email").readNullable[String](pattern(emailRegex, "error.email")) and
    (__ \ "password").readNullable[String](pattern(sha256Regex, "error.sha256")) and
    (__ \ "username").readNullable[String](pattern(usernameRegex, "error.username")) and
    (__ \ "facebookToken").readNullable[String](pattern(facebookTokenRegex, "error.facebookToken"))
  )(LoginUser.apply _)

  implicit val errorWrite = Json.writes[Error]
  implicit val emailWrite = addHref("emails",Json.writes[Email])
  implicit val topLevelWrite = Json.writes[TopLevel]

}
