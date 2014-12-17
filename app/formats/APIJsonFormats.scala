package formats

import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONObjectID

trait APIJsonFormats extends CommonJsonFormats {
 // implicit val objectIDWrite: Writes[BSONObjectID] = Writes{ (objectID: BSONObjectID) => JsString(objectID.stringify) }
  implicit val objectIDWrite: Writes[BSONObjectID] = Writes{ (objectID: BSONObjectID) => JsString(objectID.stringify) }
  implicit val objectIDRead: Reads[BSONObjectID] = (JsPath \ "$oid").read[String].map(BSONObjectID(_))

  implicit val userWrite: Writes[User] = (
    (__ \ "email").write[String] and
    (__ \ "_id").writeNullable[BSONObjectID] and
    (__ \ "state").writeNullable[String]
  )(user => (user.email, user._id, user.state))
  implicit val userRead = Json.reads[User]

  implicit val errorFormat = Json.format[Error]
  implicit val emailFormat = Json.format[Email]
  implicit val topLevelFormat = Json.format[TopLevel]
}
