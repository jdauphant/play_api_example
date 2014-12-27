package formats

import models._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import utils.Hash

trait CommonJsonFormats {
   implicit def fromNewUser(newUser: NewUser): User = new User(newUser.email,Hash.bcrypt(newUser.password))
   implicit def stringToError(s: String): Error = new Error(s)
}
