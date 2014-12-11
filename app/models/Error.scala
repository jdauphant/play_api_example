package models

import play.api.libs.json._

case class Error(title: String, id: Option[String] = None, detail: Option[String] = None)

object Error {
   val errorFormat = Json.format[Error]
}