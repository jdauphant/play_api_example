package formats

import models.User
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats

trait UserFormats {
  import BSONFormats.BSONObjectIDFormat
  implicit val userFormat = Json.format[User]
}
