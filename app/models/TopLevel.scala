package models

case class TopLevel(links: Option[Map[String,String]] = None,
                    emails: Option[Email] = None,
                    users: Option[User] = None,
                    tokens: Option[Token] = None,
                    errors: Either[Option[Error],Seq[Error]] = Left(None))
