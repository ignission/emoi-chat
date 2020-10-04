package jam.websocket.interpreters

import akka.NotUsed
import akka.stream.scaladsl.Flow
import cats.Applicative
import monix.eval.Task
import monix.execution.Scheduler

import jam.application.dsl.Result.Result
import jam.domains.Id
import jam.websocket.AppError
import jam.websocket.actors.{BroadcastIn, NewClient}
import jam.websocket.dsl.{ErrorCodes, UserDSL}
import jam.websocket.messages.{
  ErrorOccured,
  NoReply,
  UnknownMessage,
  UpdateUser,
  UserConnected,
  UserInfo,
  UserMessage,
  UserMoved
}
import jam.websocket.models.User
import jam.websocket.server.Reply

import scala.concurrent.Future

trait Interpreter[F[_]] {
  def interpreter(userDSL: UserDSL[F]): Flow[UserMessage, Reply, NotUsed]
}

trait DSLExecution {
  def execute[F[_]: Applicative, A](userId: Id[User], result: => Result[F, AppError, A])(
      f: A => Reply
  ): F[Reply] = {
    val applicativeInstance = implicitly[Applicative[F]]
    applicativeInstance.map(result) {
      case Right(a) => f(a)
      case Left(error: AppError) =>
        Reply(ErrorOccured(userId, error.code))
      case Left(_) =>
        Reply(ErrorOccured(userId, ErrorCodes.InternalError))
    }
  }
}

class InterpreterAsync(implicit s: Scheduler) extends Interpreter[Task] with DSLExecution {
  override def interpreter(userDSL: UserDSL[Task]): Flow[UserMessage, Reply, NotUsed] =
    Flow[UserMessage].mapAsync(1) {
      case msg: UnknownMessage =>
        Future.successful(Reply(msg))
      case UserConnected(userId, name, queue) =>
        execute(userId, userDSL.addUser(User.create(userId, name))) { user =>
          Reply(UserInfo(userId, user), Seq(NewClient(userId, queue)))
        }.runToFuture
      case UserMoved(userId, position) =>
        execute(userId, userDSL.updatePosition(userId, position)) { user =>
          Reply(
            NoReply,
            Seq(BroadcastIn(UpdateUser(userId, user)))
          )
        }.runToFuture
      case _ =>
        Future.successful(Reply(NoReply))
    }
}