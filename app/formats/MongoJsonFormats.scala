package formats

import models.User
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats

trait MongoJsonFormats extends CommonJsonFormats {
  import BSONFormats.BSONObjectIDFormat
  implicit val userRead = Json.reads[User]
  implicit val userWrite = Json.writes[User]
}
