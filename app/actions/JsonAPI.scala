package actions

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

case class JsonAPI[A](action: Action[A]) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    action(request).map{ result =>
      result.as("application/vnd.api+json")
    }
  }
  lazy val parser = action.parser
}