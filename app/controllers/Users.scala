package controllers

import actions.JsonAPIAction
import models._
import play.api._
import play.api.libs.json.{JsError, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._

import scala.concurrent.Future


object Users extends Controller {
  implicit val userFormat = User.userFormat
  implicit val errorFormat = Error.errorFormat
  implicit val topLevelFormat = TopLevel.topLevelFormat

  def create = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = request.body.validate[User]
    userResult.fold(
      errors => {
        Future.successful(BadRequest(Error.toTopLevelJson(Error(JsError.toFlatJson(errors).toString()))))
      },
      user => {
        User.create(user).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created(Json.toJson(TopLevel(users=Some(user))))
        }
      }
    )
  }

  def checkEmail(emailToTest: String) = JsonAPIAction.async { request =>
    User.findByEmail(emailToTest).map {
      case User(email, _, _, _) :: Nil if email == emailToTest =>
        Ok(Json.toJson(TopLevel(users=Some(User(email, state=Some("exist"))))))
      case _ =>
        NotFound(Error.toTopLevelJson(Error("No user account for this email")))
    }
  }

  def login = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = request.body.validate[User]
    userResult.fold(
      errors => {
        Future.successful(BadRequest(Error.toTopLevelJson(Error(JsError.toFlatJson(errors).toString()))))
      },
      userLogging => {
        User.findByEmail(userLogging.email).map { users => users match {
            case user :: Nil if userLogging.email == user.email && userLogging.password == user.password =>
              Ok(Json.toJson(TopLevel(users = Some(user))))
            case User(email, _, _, _) :: Nil if userLogging.email == email =>
              Unauthorized(Error.toTopLevelJson(Error("Incorrect password")))
            case _ =>
              Unauthorized(Error.toTopLevelJson(Error("No user account for this email")))
          }
        }
      }
    )
  }
}
