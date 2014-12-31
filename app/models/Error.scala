package models

import formats.APIJsonFormats
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import play.api.libs.json._


case class Error(title: String, id: Option[String] = None, detail: Option[String] = None)

object Error extends APIJsonFormats {
   def toTopLevelJson(errors: Either[Option[Error],Seq[Error]]): JsValue = Json.toJson(TopLevel(errors=errors))
   def toTopLevelJson(error: Error): JsValue = toTopLevelJson(Left(Some(error)))
   def toTopLevelJson(validationErrors: Seq[(JsPath,Seq[ValidationError])]): JsValue = toTopLevelJson(Right(validationErrors.map {
      error =>
         Error(Messages("error.field",error._1.toString(),error._2.map { er => Messages(er.message) }.mkString(",")))
   }))
}