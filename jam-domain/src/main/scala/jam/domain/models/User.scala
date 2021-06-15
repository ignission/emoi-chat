package jam.domain.models

case class User(name: UserName, position: Position)

case class UserName(value: String) extends AnyVal

object UserName {
  val anonymous: UserName =
    UserName("anonymous")
}

case class Position(x: Int, y: Int)
