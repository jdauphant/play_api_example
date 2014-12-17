package models

case class TopLevel(links: Option[Map[String,String]] = None, emails: Option[Email] = None, users: Option[User] = None, errors: Option[Error] = None)
