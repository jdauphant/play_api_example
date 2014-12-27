package formats

import models.{Token, User}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.modules.reactivemongo.json.BSONFormats

trait MongoJsonFormats extends CommonJsonFormats {
  import BSONFormats.BSONObjectIDFormat

  def mongoReads[T](r: Reads[T]): Reads[T] = {
    __.json.update((__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString] )) andThen r
  }

  def mongoWrites[T](w : Writes[T]): Writes[T] = {
    w.transform( js => js.as[JsObject] - "id"  ++ Json.obj("_id" -> Json.obj("$oid" -> js \ "id")) )
  }

  implicit val tokenRead = mongoReads[Token](Json.reads[Token])
  implicit val tokenWrite = mongoWrites[Token](Json.writes[Token])
  implicit val userRead = mongoReads[User](Json.reads[User])
  implicit val userWrite = mongoWrites[User](Json.writes[User])
}
