package net.cs.core.api


import net.cs.core.objs.CardTypes.CardType

trait Request {
  def endpoint: String
  def stringifyPayload: Option[String] = None
  def cardType: CardType
}