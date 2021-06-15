package jam.domain.models

case class Room(name: RoomName, users: Set[User])

case class RoomName(value: String) extends AnyVal
