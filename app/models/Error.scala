package models

import formats.ErrorFormats
import play.api.libs.json._

case class Error(title: String, id: Option[String] = None, detail: Option[String] = None)

object Error extends ErrorFormats {
   def toTopLevelJson(error: Error) = Json.toJson(TopLevel(errors=Some(error)))
}