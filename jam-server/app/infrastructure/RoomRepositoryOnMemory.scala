package infrastructure

import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink}
import akka.stream.{KillSwitches, Materializer, UniqueKillSwitch}
import jam.domain.models.{Room, RoomName, UserCommand, UserName}
import services.{RoomInfo, RoomRepository}

import scala.collection.mutable
import scala.concurrent.duration._

@Singleton
class RoomRepositoryOnMemory @Inject() (
    implicit val materializer: Materializer,
    implicit val system: ActorSystem
) extends RoomRepository {
  import RoomRepositoryOnMemory._

  override def room(roomName: RoomName, userName: UserName): RoomInfo = synchronized {
    roomPool.get.get(roomName) match {
      case Some(chatRoom) =>
        chatRoom
      case None =>
        val room = create(roomName)
        roomPool.get() += (roomName -> room)
        room
    }
  }

  private def create(roomName: RoomName): RoomInfo = {
    val (sink, source) =
      MergeHub
        .source[UserCommand](perProducerBufferSize = 16)
        .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
        .run()

    source.runWith(Sink.ignore)

    val channel = RoomChannel(sink, source)
    val bus: Flow[UserCommand, UserCommand, UniqueKillSwitch] = Flow
      .fromSinkAndSource(channel.sink, channel.source)
      .joinMat(KillSwitches.singleBidi[UserCommand, UserCommand])(Keep.right)
      .backpressureTimeout(3.seconds)

    RoomInfo(Room(roomName, Set()), bus)
  }
}

object RoomRepositoryOnMemory {
  private val rooms: scala.collection.mutable.Map[RoomName, RoomInfo] =
    scala.collection.mutable.Map()

  val roomPool: AtomicReference[scala.collection.mutable.Map[RoomName, RoomInfo]] =
    new AtomicReference[mutable.Map[RoomName, RoomInfo]](rooms)
}
