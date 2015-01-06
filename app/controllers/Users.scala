package controllers

import actions.JsonAPIAction

import akka.actor.ActorSystem
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spracebook._
import spracebook.Exceptions._
import akka.pattern.ask

import formats.APIJsonFormats
import models._
import play.api._
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import reactivemongo.core.errors.DatabaseException
import utils.Hash

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future

object Users extends Controller with APIJsonFormats {

  def create = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = (request.body \ "users").validate[NewUser]
    userResult.fold(
      validationErrors => {
        Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
      },
      user => user match {
        case NewUser(_, Some(email), _, None) =>
          createUserByEmail(user)
        case NewUser(_, None, _, Some(facebookToken)) =>
          createUserByFacebook(user,facebookToken)
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(s"You can specified only passwordHash or facebookToken to create an user")))
      }
    )
  }

  def createUserByEmail(user: NewUser): Future[Result] = {
    val newUser = User.fromNewUser(user)
    createUser(newUser)
  }

  def createUserByFacebook(user: NewUser, facebookToken: String): Future[Result] = {
    retrieveFacebookUserId(facebookToken).flatMap{
      facebookUserId =>
        val newUser = User.fromNewUser(user, Some(facebookUserId))
        createUser(newUser)
    }.recover {
      case exception: FacebookException if exception.exceptionType == "OAuthException" =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case InvalidAccessTokenException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case AccessTokenExpiredException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Expired facebook access token")))
    }
  }

  def createUser(user: User): Future[Result] = {
    User.create(user).map { lastError =>
      Logger.debug(s"Successfully inserted with LastError: $lastError")
      val token = Token.newTokenForUser(user.id)
      Created(Json.toJson(TopLevel(users=Some(user), tokens = Some(token))))
    }.recover {
      case exception: DatabaseException if exception.code.contains(11000) && exception.getMessage().contains("emailUniqueIndex")=>
        Logger.debug("email already exist with database: "+exception.getMessage())
        Conflict(Error.toTopLevelJson(s"An user with email ${user.email} already exists"))
      case exception: DatabaseException if exception.code.contains(11000) && exception.getMessage().contains("usernameUniqueIndex")=>
        Logger.debug("username already exist with database: "+exception.getMessage())
        Conflict(Error.toTopLevelJson(s"An user with username ${user.username} already exists"))
      case exception: DatabaseException if exception.code.contains(11000) && exception.getMessage().contains("facebookUserIdUniqueIndex")=>
        Logger.debug("username already exist with database: "+exception.getMessage())
        Conflict(Error.toTopLevelJson(s"An user with facebookUserId ${user.facebookUserId.get} already exists"))
    }
  }

  def checkEmail(email: String) = JsonAPIAction.async { request =>
    User.findByEmail(email).map {
      case User(`email`, _, _, _, _, _, _) :: Nil =>
        Ok(Json.toJson(TopLevel(emails=Some(Email(email,"registered")))))
      case _ =>
        NotFound(Error.toTopLevelJson(Error("Email not found")))
    }
  }

  def login = JsonAPIAction.async(BodyParsers.parse.tolerantJson) { request =>
    val userResult = (request.body \ "users").validate[LoginUser]
    userResult.fold(
      validationErrors => {
        Future.successful(BadRequest(Error.toTopLevelJson(validationErrors)))
      },
      loginUser => loginUser match {
        case LoginUser(Some(email), Some(passwordHash), None, None) =>
          loginByEmail(email, passwordHash)
        case LoginUser(None, Some(passwordHash), Some(username), None) =>
          loginByUsername(username, passwordHash)
        case LoginUser(None, None, None, Some(facebookToken)) =>
          loginByFacebook(facebookToken)
        case _ =>
          Future.successful(BadRequest(Error.toTopLevelJson(s"You can specified only username/passwordHash, email/passwordHash or facebookToken to login")))
      }
    )
  }

  def loginByEmail(email: String, loginPasswordHash: String): Future[Result] = User.findByEmail(email).map {
    users => users match {
      case User(`email`, Some(passwordHash), id, _, _,_, _) :: Nil if Hash.bcrypt_compare(loginPasswordHash,passwordHash) =>
        val token = Token.newTokenForUser(id)
        Ok(Json.toJson(TopLevel(users = Some(users.head), tokens = Some(token) )))
      case User(`email`, None, _, _, Some(_), Some(_), _) :: Nil =>
        Unauthorized(Error.toTopLevelJson(Error("User logged by facebook")))
      case User(`email`, _, _, _, _, _, _) :: Nil =>
        Unauthorized(Error.toTopLevelJson(Error("Incorrect password")))
      case _ =>
        NotFound(Error.toTopLevelJson(Error("No user account for this email")))
    }
  }

  def loginByUsername(username: String, loginPasswordHash: String): Future[Result] = User.findByUsername(username).map {
    users => users match {
      case User(_, Some(passwordHash), id, `username`, _, _, _) :: Nil if Hash.bcrypt_compare(loginPasswordHash,passwordHash) =>
        val token = Token.newTokenForUser(id)
        Ok(Json.toJson(TopLevel(users = Some(users.head), tokens = Some(token) )))
      case User(_, None, _, `username`, Some(_), Some(_), _) :: Nil =>
        Unauthorized(Error.toTopLevelJson(Error("User registered by facebook")))
      case User(_, _, _, `username`, _, _, _) :: Nil =>
        Unauthorized(Error.toTopLevelJson(Error("Incorrect password")))
      case _ =>
        NotFound(Error.toTopLevelJson(Error("No user account for this username")))
    }
  }

  def retrieveFacebookUserId(facebookToken: String): Future[String] = {
    implicit val system = ActorSystem()
    implicit val timeout = Timeout(10 seconds)

    val facebook: Future[SprayClientFacebookGraphApi] = for {
      Http.HostConnectorInfo(connector, _) <- IO(Http) ? Http.HostConnectorSetup("graph.facebook.com", 443, true)
    } yield {
      new SprayClientFacebookGraphApi(connector)
    }

    facebook.flatMap(_.getUser(facebookToken)).map {
      user =>
        Logger.debug(s"Retrieve user with id ${user.id} from facebook graph api")
        user.id
    }
  }

  def loginByFacebook(facebookToken: String): Future[Result] = {
    retrieveFacebookUserId(facebookToken).flatMap{
      facebookUserId =>
        User.findByFacebookUserId(facebookUserId).map {
          users => users match {
            case User(_, _, id, _, _, Some(`facebookUserId`), _) :: Nil =>
              val token = Token.newTokenForUser(id)
              Ok(Json.toJson(TopLevel(users = Some(users.head), tokens = Some(token))))
            case _ =>
              NotFound(Error.toTopLevelJson(Error("No user account for the facebookUserId associated to this facebookToken")))
          }
        }
    }.recover {
      case exception: FacebookException if exception.exceptionType == "OAuthException" =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case InvalidAccessTokenException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Invalid facebook access token")))
      case AccessTokenExpiredException(_,_,_,_) =>
        Unauthorized(Error.toTopLevelJson(Error("Expired facebook access token")))
    }
  }

  val access_token_header = "X-Access-Token"
  def get(id: String) = JsonAPIAction.async { request =>
    request.headers.get(access_token_header) match {
      case None =>
        Future.successful(Unauthorized(Error.toTopLevelJson(Error(s"No token provided : use the Header '$access_token_header'"))))
      case Some(access_token) =>
        Token.findById(access_token).flatMap {
          case Token(`id`, _) :: Nil =>
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
