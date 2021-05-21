package domain.models

sealed trait UserCommand

object UserCommand {
  case class Join(room: Room, user: User)                 extends UserCommand
  case class Leave(userName: UserName)                    extends UserCommand
  case class Move(userName: UserName, position: Position) extends UserCommand
}
