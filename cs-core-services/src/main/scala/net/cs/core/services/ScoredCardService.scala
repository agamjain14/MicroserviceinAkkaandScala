package net.cs.core.services

import akka.actor.Props
import akka.pattern.pipe
import net.cs.core.api.Request
import net.cs.core.api.actors.EntityServiceActor
import net.cs.core.api.descriptors.{EntityServiceConfig, EntityServiceDescriptor}
import net.cs.core.objs.CardTypes.CardType
import net.cs.core.objs.Cards.{CreditCard, ScoredCard}
import net.cs.core.objs.{CardTypes, CardUser}
import net.cs.core.servicemessages.ScoredCardServiceMessage
import net.cs.core.servicemessages.ScoredCardServiceMessage.GetScoredCards

import scala.concurrent.Future

object ScoredCardService extends EntityServiceDescriptor {


  final val name = "scored-card-service"

  final val entityName = "ScoredCard"


  override def props(c: EntityServiceConfig)(entityId: String) = {
    Props(classOf[ScoredCardService], entityId, c)
  }

  override def idExtractor(c: EntityServiceConfig) = {
    case m: ScoredCardServiceMessage => m.userId //the entity id
    case _                      => this.defaultEntityId
  }

  override def shardResolver(c: EntityServiceConfig)(entityId: String) = entityId.hashCode() % c.nbShards

}

class ScoredCardService(override final val entityId: String, override final val serviceConfig: EntityServiceConfig)
  extends EntityServiceActor {

  import context.dispatcher


  override final val serviceDescriptor = ScoredCardService

  override def preStart(): Unit = {
    super.preStart()
    log.info("ScoredCard service started")
  }

  override def receive: Receive = {

    case GetScoredCards(userId: String, user: CardUser, client) => {
      //println("in GetScoredCards")
      val request = new Request {
        override def endpoint: String = "https://app.clearscore.com/api/global/backend-tech-test/v2/creditcards"

        override def cardType: CardType = CardTypes.ScoredCardType

        override def stringifyPayload: Option[String] = Some(
          s"""
             |{
             |"name": "${user.name.toString}",
             |"score": ${user.creditScore},
             |"salary": ${user.salary}
             |}
       """.stripMargin
        )
      }

      val futureCards: Future[Seq[CreditCard]] = client.getCards(request).map { cards =>
        cards.collect {
          case c: ScoredCard =>
            val score  = c.approvalRating * Math.pow(1/c.apr, 2) * 100
            CreditCard("ScoredCards", c.card, c.apr, f"$score%1.3f".toDouble)
        }
      }

      futureCards pipeTo sender()
    }
  }
}