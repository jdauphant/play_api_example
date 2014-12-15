package formats

import models.Error
import play.api.libs.json.Json

trait ErrorFormats {
   implicit val errorFormat = Json.format[Error]
}
