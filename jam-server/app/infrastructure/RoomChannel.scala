package infrastructure

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import jam.domain.models.UserCommand

case class RoomChannel(
    sink: Sink[UserCommand, NotUsed],
    source: Source[UserCommand, NotUsed]
)
