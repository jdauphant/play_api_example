package controllers

import actions.JsonAPIAction
import formats.APIJsonFormats
import models._
import play.api._
import play.api.libs.json.{JsError, Json}
import play.api.mvc.Results._
import reactivemongo.core.errors.DatabaseException
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._

import scala.concurrent.Future


object Users extends Controller with APIJsonFormats {

  def create = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = request.body.validate[NewUser]
    userResult.fold(
      errors => {
        Future.successful(BadRequest(Error.toTopLevelJson(JsError.toFlatJson(errors).toString())))
      },
      user => {
        val newUser = User.fromNewUser(user)
        User.create(newUser).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          val token = Token.newTokenForUser(newUser.id)
          Created(Json.toJson(TopLevel(users=Some(newUser))))
        }.recover {
          case exception: DatabaseException if exception.code.contains(11000) => Conflict(Error.toTopLevelJson(s"An user with email ${user.email} already exists"))
          case e: Throwable => Logger.error(s"Impossible to create user ${user.email} : $e.getMessage")
            InternalServerError(Error.toTopLevelJson(Error("Internal api error, try later")))
        }
      }
    )
  }

  def checkEmail(emailToTest: String) = JsonAPIAction.async { request =>
    User.findByEmail(emailToTest).map {
      case User(email, _, _, _) :: Nil if email == emailToTest =>
        Ok(Json.toJson(TopLevel(emails=Some(Email(email,"registered")))))
      case _ =>
        NotFound(Error.toTopLevelJson(Error("Email not found")))
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
            case User(email, passwordHash, id, _) :: Nil if userLogging.email == email && Hash.bcrypt_compare(userLogging.password,passwordHash) =>
              val token = Token.newTokenForUser(id)
              Ok(Json.toJson(TopLevel(users = Some(users.head), tokens = Some(token) )))
            case User(email, _, _, _) :: Nil if userLogging.email == email =>
              NotFound(Error.toTopLevelJson(Error("Incorrect password")))
            case _ =>
              Unauthorized(Error.toTopLevelJson(Error("No user account for this email")))
          }
        }
      }
    )
  }
}
