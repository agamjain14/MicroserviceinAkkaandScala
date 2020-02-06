package net.cs.core.services

import akka.actor.Props
import akka.pattern.pipe
import net.cs.core.api.Request
import net.cs.core.api.actors.EntityServiceActor
import net.cs.core.api.descriptors.{EntityServiceConfig, EntityServiceDescriptor}
import net.cs.core.objs.CardTypes.CardType
import net.cs.core.objs.Cards.{CSCard, CreditCard}
import net.cs.core.objs.{CardTypes, CardUser}
import net.cs.core.servicemessages.CSCardServiceMessage
import net.cs.core.servicemessages.CSCardServiceMessage.GetCSCards

import scala.concurrent.Future

object CSCardService extends EntityServiceDescriptor {

  final val name = "cs-card-service"

  final val entityName = "CSCard"


  override def props(c: EntityServiceConfig)(entityId: String) = {
    Props(classOf[CSCardService], entityId, c)
  }

  override def idExtractor(c: EntityServiceConfig) = {
    case m: CSCardServiceMessage => m.userId //the entity id
    case _                      => this.defaultEntityId
  }

  override def shardResolver(c: EntityServiceConfig)(entityId: String) = entityId.hashCode() % c.nbShards

 // case class CSCardResponse(provider: String, name: String, apr: String, cardScore: String)
}

class CSCardService(override final val entityId: String, override final val serviceConfig: EntityServiceConfig) extends EntityServiceActor{

  override final val serviceDescriptor = CSCardService

  import context.dispatcher


  override def preStart(): Unit = {
    super.preStart()
    log.info("CSCard service started")
  }

  override def receive: Receive = {

    case GetCSCards(userId: String, user: CardUser, client) => {
      val request = new Request {
        override def endpoint: String = "https://app.clearscore.com/api/global/backend-tech-test/v1/cards"

        override def cardType: CardType = CardTypes.CSCardType

        override def stringifyPayload: Option[String] = Some(
          s"""
             |{
             |"name": "${user.name.toString}",
             |"creditScore": ${user.creditScore}
             |}
       """.stripMargin
        )
      }

      val futureCards: Future[Seq[CreditCard]] = client.getCards(request).map { cards =>
        cards.collect {
          case c: CSCard =>
            val score  = c.eligibility * Math.pow(1/c.apr, 2) * 10
            CreditCard("CSCards", c.cardName, c.apr, f"$score%1.3f".toDouble)
          }
        }
      futureCards pipeTo sender()
    }
  }

}
