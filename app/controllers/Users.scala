package controllers

import actions.JsonAPIAction
import formats.APIJsonFormats
import models._
import play.api._
import play.api.i18n.Messages
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
      validationErrors => {
        Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
      },
      user => {
        val newUser = User.fromNewUser(user)
        User.create(newUser).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          val token = Token.newTokenForUser(newUser.id)
          Created(Json.toJson(TopLevel(users=Some(newUser), tokens = Some(token))))
        }.recover {
          case exception: DatabaseException if exception.code.contains(11000) =>
            Logger.debug("User already exist with database: "+exception.getMessage())
            Conflict(Error.toTopLevelJson(s"An user with email ${user.email} or username ${user.username} already exists"))
        }
      }
    )
  }

  def checkEmail(emailToTest: String) = JsonAPIAction.async { request =>
    User.findByEmail(emailToTest).map {
      case User(email, _, _, _, _) :: Nil if email == emailToTest =>
        Ok(Json.toJson(TopLevel(emails=Some(Email(email,"registered")))))
      case _ =>
        NotFound(Error.toTopLevelJson(Error("Email not found")))
    }
  }

  def login = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = request.body.validate[LoginUser]
    userResult.fold(
      validationErrors => {
        Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
      },
      userLogging => userLogging match {
        case LoginUser(Some(loginEmail), loginPasswordHash, None) =>
        loginByEmail(loginEmail, loginPasswordHash)
        case LoginUser(None, loginPasswordHash, Some(loginUsername)) =>
        loginByUsername(loginUsername, loginPasswordHash)
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(s"You can specified only username or email to login")))
      }
    )
  }

  def loginByEmail(loginEmail: String, loginPasswordHash: String): Future[Result] = User.findByEmail(loginEmail).map {
    users => users match {
      case User(email, passwordHash, id, _, _) :: Nil if loginEmail == email && Hash.bcrypt_compare(loginPasswordHash,passwordHash) =>
        val token = Token.newTokenForUser(id)
        Ok(Json.toJson(TopLevel(users = Some(users.head), tokens = Some(token) )))
      case User(email, _, _, _, _) :: Nil if loginEmail == email =>
        NotFound(Error.toTopLevelJson(Error("Incorrect password")))
      case _ =>
        Unauthorized(Error.toTopLevelJson(Error("No user account for this email")))
    }
  }

  def loginByUsername(loginUsername: String, loginPasswordHash: String): Future[Result] = User.findByUsername(loginUsername).map {
    users => users match {
      case User(_, passwordHash, id, username, _) :: Nil if username == loginUsername && Hash.bcrypt_compare(loginPasswordHash,passwordHash) =>
        val token = Token.newTokenForUser(id)
        Ok(Json.toJson(TopLevel(users = Some(users.head), tokens = Some(token) )))
      case User(_, _, _, username, _) :: Nil if username == loginUsername =>
        NotFound(Error.toTopLevelJson(Error("Incorrect password")))
      case _ =>
        Unauthorized(Error.toTopLevelJson(Error("No user account for this username")))
    }
  }

  val access_token_header = "X-Access-Token"
  def get(id: String) = JsonAPIAction.async { request =>
    request.headers.get(access_token_header) match {
      case None =>
        Future.successful(Unauthorized(Error.toTopLevelJson(Error(s"No token provided : use the Header '$access_token_header'"))))
      case Some(access_token) =>
        Token.findById(access_token).flatMap {
          case Token(userId, _) :: Nil if id == userId =>
            User.findById(id).map {
              case user :: Nil =>
                Ok(Json.toJson(TopLevel(users = Some(user))))
              case _ =>
                NotFound(Error.toTopLevelJson(Error(s"User $id not found")))
            }
          case Token(tokenUserId, _)  :: Nil if id != tokenUserId =>
            Future.successful(Forbidden(Error.toTopLevelJson(Error("You can only retrieve the user associated with the token"))))
          case _ =>
            Future.successful(Unauthorized(Error.toTopLevelJson(Error(s"Unknown token $access_token"))))
        }
    }
  }
}
