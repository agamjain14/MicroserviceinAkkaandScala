package net.cs.core.servicemessages

import net.cs.core.akkaclient.AkkaClient
import net.cs.core.objs.CardUser
import net.cs.core.objs.Cards.ScoredCard


object ScoredCardServiceMessage {

  case class GetScoredCards(userId: String, user: CardUser, client: AkkaClient) extends EventServiceMessage with ScoredCardServiceMessage

}

sealed trait ScoredCardServiceMessage {
  this: ServiceMessage =>
  def userId: String
}
