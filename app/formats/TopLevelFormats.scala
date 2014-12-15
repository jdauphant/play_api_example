package formats


import models._
import play.api.libs.json.Json

trait TopLevelFormats extends ErrorFormats with UserFormats
{
  implicit val topLevelFormat = Json.format[TopLevel]
}
