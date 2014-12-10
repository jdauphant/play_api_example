import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import models.{TopLevel, Error}

import scala.concurrent.Future

object Global extends GlobalSettings {

  def errorStringToJson(error: String) = Json.toJson(TopLevel(errors=Some(Error(error))))

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(errorStringToJson(error)).as("application/vnd.api+json"))
  } 

  override def onError(request: RequestHeader, throwable: Throwable) = {
    Future.successful(InternalServerError(errorStringToJson("Internal api error, try later")).as("application/vnd.api+json"))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(errorStringToJson("Handler not found, check the API documentation")).as("application/vnd.api+json"))
  }

}