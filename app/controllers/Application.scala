package controllers

import actions.JsonAPI
import formats.APIJsonFormats
import models.TopLevel
import play.api.libs.json.Json
import play.api.mvc._

object Application extends Controller with APIJsonFormats {

  def index = JsonAPI{ Action {
      val services = Map( "users"-> "/users",
                          "emails"-> "/emails",
                          "tokens"-> "/tokens"
      )
      Ok(Json.toJson(TopLevel(links=Some(services))))
    }
  }

}
