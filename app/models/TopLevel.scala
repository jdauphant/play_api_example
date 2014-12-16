package models

import play.api.libs.json._

case class TopLevel(links: Option[Map[String,String]] = None, users: Option[User] = None, errors: Option[Error] = None)
