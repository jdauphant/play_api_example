package controllers

import models.TopLevel
import play.api.libs.json.Json
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    val services = Map("users"-> "/users")
  	Ok(Json.toJson(TopLevel(links=Some(services)))).as("application/vnd.api+json")
  }

}
