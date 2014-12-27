package models

import reactivemongo.bson.BSONObjectID

case class NewUser(email: String, password: String)