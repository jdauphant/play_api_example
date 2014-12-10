package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TopLevel(links: Option[Map[String,String]] = None, errors: Option[Error] = None)

object TopLevel {
  implicit val errorWrites = Error.errorWrites
  implicit val topLevelWrites: Writes[TopLevel] = (
      (JsPath \ "links").writeNullable[Map[String,String]] and
      (JsPath \ "errors").writeNullable[Error]
    )(unlift(TopLevel.unapply))
}