package actors

import akka.actor.{Actor, ActorRef, Props}
import infrastructure.RedisClient
import jam.domain.models.{Position, Room, RoomName, User, UserCommand, UserName}
import play.api.libs.json.{JsValue, Json}

class RoomRequestActor(out: ActorRef, redis: RedisClient, roomName: RoomName, userName: UserName)
    extends Actor {
  import formatters.PlayJsonFormats._
  import UserCommand._

  override def receive: Receive = {
    case msg: JsValue =>
      (msg \ "command").as[String] match {
        case "move" =>
          val req = msg.as[Move]
          out ! Move(req.userName, req.position)
        case "chat" =>
          val req = msg.as[Chat]
          out ! Chat(req.userName, req.message)
        case "ping" =>
          out ! Ping
        case others =>
          throw new IllegalArgumentException(s"Invalid command: $others")
      }
  }

  override def preStart(): Unit = {
    val user     = User(userName, Position(100, 100))
    val allUsers = redis.getAll.map(Json.parse).map(_.as[User])
    redis.put(userName.value, Json.toJson(user).toString())
    out ! Join(Room(roomName, (allUsers :+ user).toSet), user)
  }

  override def postStop(): Unit = {
    redis.delete(userName.value)
//    out ! Leave(userName)
//    out ! PoisonPill
  }
}

object RoomRequestActor {
  def props(
      out: ActorRef,
      redisClient: RedisClient,
      roomName: RoomName,
      userName: UserName
  ): Props = Props(
    new RoomRequestActor(out, redisClient, roomName, userName)
  )
}
