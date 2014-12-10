package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.Hash

case class Error(title: String, id: Option[String] = None, detail: Option[String] = None)

object Error {
  implicit val errorWrites: Writes[Error] = (
      (JsPath \ "title").write[String] and
      (JsPath \ "id").writeNullable[String] and
      (JsPath \ "detail").writeNullable[String]
    )(unlift(Error.unapply))
}