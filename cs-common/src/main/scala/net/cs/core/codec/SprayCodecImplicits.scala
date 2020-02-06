package net.cs.core.codec


import net.cs.core.objs.Cards.{CSCard, ScoredCard}
import spray.json.DefaultJsonProtocol

object SprayCodecImplicits extends DefaultJsonProtocol{

  implicit val csCards = jsonFormat3(CSCard)

  implicit val scoredCards = jsonFormat3(ScoredCard)
}