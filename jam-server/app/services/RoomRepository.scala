package services

import akka.stream.UniqueKillSwitch
import akka.stream.scaladsl.Flow
import jam.domain.models.{Room, RoomName, UserCommand, UserName}

case class RoomInfo(room: Room, bus: Flow[UserCommand, UserCommand, UniqueKillSwitch])

trait RoomRepository {
  def room(roomName: RoomName, userName: UserName): RoomInfo
}
