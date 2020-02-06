package net.cs.core.client.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import net.cs.core.client.HttpServerActor.{CardResponse, CreditCardFailure, CreditCardResponse, GetCards}
import net.cs.core.client.codec.JsonSupport
import net.cs.core.objs.CardUser

import scala.concurrent.Future
import scala.concurrent.duration._

trait CardsRoute extends JsonSupport {

  def httpServerActor: ActorRef
  implicit lazy val timeout = Timeout(10.seconds)

  lazy val cardsRoute: Route =
    post {
      path("creditcards") {
        entity(as[CardUser]) { user =>
          val creditCards: Future[CardResponse] =
            (httpServerActor ? GetCards(user)).mapTo[CardResponse]
          onSuccess(creditCards) {
            case c: CreditCardResponse => complete(StatusCodes.Created, c)
            case CreditCardFailure(exception) => complete(StatusCodes.InternalServerError, exception)
          }
        }
      }
    }
}
