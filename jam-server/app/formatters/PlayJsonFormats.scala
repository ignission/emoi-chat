package formatters

import jam.domain.models.{Position, User, UserCommand, UserName}
import play.api.libs.functional.syntax._
import play.api.libs.json._

object PlayJsonFormats {
  import UserCommand._

  implicit val chatCommandReads: Reads[Chat] = (
    (JsPath \ "userName").read[String].map(UserName(_)) and
      (JsPath \ "message").read[String]
  )(Chat)

  implicit val positionReads: Reads[Position] = (
    (JsPath \ "x").read[Int] and
      (JsPath \ "y").read[Int]
  )(Position)

  implicit val moveCommandReads: Reads[Move] = (
    (JsPath \ "userName").read[String].map(UserName(_)) and
      (JsPath \ "position").read[Position]
  )(Move)

  implicit val positionWrites: Writes[Position] =
    (o: Position) =>
      Json.obj(
        "x" -> o.x,
        "y" -> o.y
      )

  implicit val userReads: Reads[User] = (
    (JsPath \ "name").read[String].map(UserName(_)) and
      (JsPath \ "position").read[Position]
  )(User)

  implicit val userNameWrites: Writes[UserName] =
    (o: UserName) => JsString(o.value)

  implicit val userWrites: Writes[User] =
    (o: User) =>
      Json.obj(
        "name"     -> o.name,
        "position" -> o.position
      )

  implicit val moveCommandWrites: Writes[Move] =
    (o: Move) =>
      Json.obj(
        "command" -> "move",
        "user"    -> Json.toJson(User(o.userName, o.position))
      )

  implicit val chatCommandWrites: Writes[Chat] =
    (o: Chat) =>
      Json.obj(
        "command"  -> "chat",
        "userName" -> Json.toJson(o.userName),
        "message"  -> Json.toJson(o.message)
      )

  implicit val joinCommandWrites: Writes[Join] =
    (o: Join) =>
      Json.obj(
        "command" -> "join",
        "users"   -> Json.toJson(o.room.users)
      )

  implicit val leaveCommandWrites: Writes[Leave] =
    (o: Leave) =>
      Json.obj(
        "command"  -> "leave",
        "userName" -> o.userName
      )
}
