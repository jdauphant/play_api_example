package actions

import controllers.Application
import formats.APIJsonFormats
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object JsonAPIAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    block(request).map{ result =>
      result.as("application/vnd.api+json")
    }
  }
}