package net.cs.core.client


import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import net.cs.core.akkaclient.AkkaClient
import net.cs.core.akkaclient.AkkaClient.CustomException
import net.cs.core.api.CoreServiceRunner
import net.cs.core.client.HttpServerActor.{CreditCardFailure, CreditCardResponse, GetCards}
import net.cs.core.client.routes.CardsRoute
import net.cs.core.objs.Cards.CreditCard
import net.cs.core.objs.{CardUser, Cards}
import net.cs.core.servicemessages.CSCardServiceMessage.GetCSCards
import net.cs.core.servicemessages.ScoredCardServiceMessage.GetScoredCards
import net.cs.core.util.SortingUtility.Sorter

import scala.concurrent.ExecutionContext



object HttpServerActor {
  def props(runner: CoreServiceRunner, client: AkkaClient, config: Config)(implicit system: ActorSystem, ec: ExecutionContext, m: ActorMaterializer) = Props(new HttpServerActor(runner, client, config))

  case class GetCards(user: CardUser)

  sealed trait CardResponse

  case class CreditCardResponse(cards: Seq[CreditCard]) extends CardResponse

  case class CreditCardFailure(exception: String) extends CardResponse
}

class HttpServerActor(runner: CoreServiceRunner, client: AkkaClient, config: Config)(implicit system: ActorSystem, ec: ExecutionContext, m: ActorMaterializer) extends Actor with ActorLogging with CardsRoute {

  import net.cs.core.util.SortingUtility.SortableInstances._
  import net.cs.core.util.SortingUtility.SortableInterface._

  private val csProxyActor = runner.service("cs-card-service")
  private val scoredProxyActor = runner.service("scored-card-service")
  private var _actor: ActorRef = _
  private lazy val routes: Route = cardsRoute

  implicit val sortByScore: Sorter[CreditCard] = (c1, c2) => c1.cardScore > c2.cardScore

  private val cardMap = scala.collection.mutable.Map.empty[CardUser, Seq[CreditCard]]
  private val bindingFuture = Http().bindAndHandle(routes, config.getString("application.api.host"), config.getInt("application.api.port"))

  override def httpServerActor: ActorRef = _actor

  override def preStart(): Unit = {
    super.preStart()
    log.info(s"started HttpServerActor Actor  at http://${config.getString("application.api.host")}:${config.getInt("application.api.port")}/")
    _actor = self

  }

  override def postStop(): Unit = {
    bindingFuture
      .flatMap(_.unbind()) // TRIGGER UNBINDING FROM THE PORT
      .onComplete(_=>system.terminate())

    super.postStop()
  }

  override def receive: Receive = {

    case GetCards(user) =>
      cardMap.put(user, Seq())
      csProxyActor !  GetCSCards("124", user, client)
      context.become(enhanceMap(user, sender()))
  }

  def enhanceMap(user: CardUser, replyTo: ActorRef): Receive = {
    case response: Seq[Cards.CreditCard] =>
      updateCardMap(user, response)
      scoredProxyActor !  GetScoredCards("124", user, client)
      context.become(enhanceMapMore(user, replyTo))

    case Failure(exception: CustomException)  =>
      cardMap -= user
      replyTo ! CreditCardFailure(exception.getMessage)
      context.become(receive)
  }

  def enhanceMapMore(user: CardUser, replyTo: ActorRef): Receive = {
    case response: Seq[Cards.CreditCard] =>
      updateCardMap(user, response)
      val a = cardMap(user).sortSeq
      //println("a "+ a)
      replyTo ! CreditCardResponse(a)
      context.become(receive)

    case Failure(exception: CustomException)  =>
      cardMap -= user
      replyTo ! CreditCardFailure(exception.getMessage)
      context.become(receive)
  }

  private def updateCardMap(user: CardUser, newCards: Seq[CreditCard]): Unit = {
    //println("newCards "+ newCards)
    cardMap.get(user) match {
      case Some(c: Seq[CreditCard]) => cardMap.update(user, c ++ newCards)
      case None => cardMap.put(user, newCards)
    }
  }

}
