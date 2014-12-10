package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.Hash

/**
 * Created by julien on 12/10/14.
 */
case class Error(title: String, id: Option[String] = None, detail: Option[String] = None)

object Error {
  implicit val errorWrites = new Writes[Error] {
    def writes(error: Error) = Json.obj(
      "title" -> error.title,
      "id" -> error.id,
      "detail" -> error.detail
    )
  }
}