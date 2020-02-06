package net.cs.core.servicemessages

import net.cs.core.akkaclient.AkkaClient
import net.cs.core.objs.CardUser

object CSCardServiceMessage {

  case class GetCSCards(userId: String, user: CardUser, client: AkkaClient) extends EventServiceMessage with CSCardServiceMessage



}

sealed trait CSCardServiceMessage {
  this: ServiceMessage =>
  def userId: String
}
