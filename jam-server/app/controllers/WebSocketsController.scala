package controllers

import javax.inject._
import actors.{RoomRequestActor, RoomResponseActor}
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import infrastructure.RedisClient
import jam.domain.models.{RoomName, UserCommand, UserName}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.RoomService

@Singleton
class WebSocketsController @Inject() (
    val controllerComponents: ControllerComponents,
    redisClient: RedisClient,
    service: RoomService
)(implicit
    system: ActorSystem
) extends BaseController {

  private val logger = play.api.Logger(getClass)

  def ws(roomName: String): WebSocket = WebSocket.accept[JsValue, JsValue] { implicit request =>
    val userName = request
      .queryString("user_name")
      .headOption
      .map(UserName(_))
      .getOrElse(UserName.anonymous)
    val rm = RoomName(roomName)

    logger.info(s"User: ${userName.value} is trying to enter room: $roomName")

    val userInput: Flow[JsValue, UserCommand, _] =
      ActorFlow.actorRef[JsValue, UserCommand](out =>
        RoomRequestActor.props(out, redisClient, rm, userName)
      )
    val roomInfo = service.start(roomName = rm, userName = userName)
    val userOutPut: Flow[UserCommand, JsValue, _] =
      ActorFlow.actorRef[UserCommand, JsValue](out =>
        RoomResponseActor.props(out, redisClient, userName)
      )

    userInput.viaMat(roomInfo.bus)(Keep.right).viaMat(userOutPut)(Keep.right)
  }
}
