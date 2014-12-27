package formats

import models.{Token, User}
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats

trait MongoJsonFormats extends CommonJsonFormats {
  import BSONFormats.BSONObjectIDFormat
  implicit val tokenWrite = Json.format[Token]
  implicit val userRead = Json.reads[User]
  implicit val userWrite = Json.writes[User]
}
