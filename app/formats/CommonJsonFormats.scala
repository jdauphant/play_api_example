package formats

import models._
import play.api.libs.json.Json
import utils.Hash

trait CommonJsonFormats {
   implicit val newUserFormat = Json.format[NewUser]
   implicit def fromNewUser(newUser: NewUser) = {
      new User(newUser.email,newUser.password.map(Hash.bcrypt(_)))
   }
}
