import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import models.Error

import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(Json.toJson(Error(error))).as("application/vnd.api+json"))
  } 

  override def onError(request: RequestHeader, throwable: Throwable) = {
    Future.successful(InternalServerError(Json.toJson(Error("Internal api error, try later"))).as("application/vnd.api+json"))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(Json.toJson(Error("Handler not found, check the API documentation"))).as("application/vnd.api+json"))
  }

}