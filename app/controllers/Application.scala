package controllers

import models.TopLevel
import play.api._
import play.api.libs.json.Json
import play.api.mvc._

object Application extends Controller {

  def index = Action {
  	Ok(Json.toJson(TopLevel(links=Some(Map.empty)))).as("application/vnd.api+json")
  }

}
