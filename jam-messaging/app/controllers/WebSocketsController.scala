package controllers

import javax.inject._

import actors.WebSocketActor
import akka.actor.ActorSystem
import infrastructure.RedisClient
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._

@Singleton
class WebSocketsController @Inject() (
    val controllerComponents: ControllerComponents,
    redis: RedisClient
)(implicit
    system: ActorSystem
) extends BaseController {

//  private val logger = play.api.Logger(getClass)

  def ws(userName: String): WebSocket = WebSocket.accept[JsValue, JsValue] { _ =>
    ActorFlow.actorRef { actorRef =>
      WebSocketActor.props(actorRef, redis, userName)
    }
  }
}