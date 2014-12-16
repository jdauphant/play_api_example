package models

case class NewUser(email: String, password: Option[String]= None)
