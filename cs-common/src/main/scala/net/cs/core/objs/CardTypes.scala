package net.cs.core.objs

object CardTypes {

  sealed trait CardType

  case object CSCardType extends CardType

  case object ScoredCardType extends CardType
}
