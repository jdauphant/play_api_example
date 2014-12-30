package models

import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.Random

case class Token(userId: String, id: String)

object Token extends MongoModel("tokens") {
  val TOKEN_ID_SIZE = 64
  private def generatedId = Random.alphanumeric.take(TOKEN_ID_SIZE).mkString
  def newTokenForUser(userId: String) = {
    val token = new Token(userId, Token.generatedId)
    val futureToken = create(token)
    token
  }

  def create(token: Token) = collection.insert(token)
  def findById(id: String) = collection.find(Json.obj("_id" -> id)).cursor[Token].collect[List]()
}