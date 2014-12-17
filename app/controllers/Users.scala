package controllers

import actions.JsonAPIAction
import formats.APIJsonFormats
import models._
import play.api._
import play.api.libs.json.{JsError, Json}
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._

import scala.concurrent.Future


object Users extends Controller with APIJsonFormats {

  def create = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = request.body.validate[NewUser]
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
    val userResult = request.body.validate[NewUser]
    userResult.fold(
      errors => {
        Future.successful(BadRequest(Error.toTopLevelJson(Error(JsError.toFlatJson(errors).toString()))))
      },
      userLogging => {
        User.findByEmail(userLogging.email).map { users => users match {
            case User(email, Some(passwordHash), _, _) :: Nil if userLogging.email == email && Hash.bcrypt_compare(userLogging.password.get,passwordHash) =>
              Ok(Json.toJson(TopLevel(users = Some(users.head))))
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
