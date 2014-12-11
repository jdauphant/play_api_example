package models

import play.api.libs.json._

case class TopLevel(links: Option[Map[String,String]] = None, errors: Option[Error] = None)

object TopLevel {
  implicit val errorFormat = Error.errorFormat
  implicit val topLevelFormat = Json.format[TopLevel]
}