package controllers

import actions.JsonAPIAction
import formats.APIJsonFormats
import models.TopLevel
import play.api.libs.json.Json
import play.api.mvc._

object Application extends Controller with APIJsonFormats {

  def index = JsonAPIAction {
    val services = Map( "users"-> "/users",
                        "emails"-> "/emails",
                        "tokens"-> "/tokens"
    )
  	Ok(Json.toJson(TopLevel(links=Some(services))))
  }

}
