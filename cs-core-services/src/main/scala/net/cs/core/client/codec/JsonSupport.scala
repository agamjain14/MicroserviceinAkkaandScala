package net.cs.core.client.codec

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import net.cs.core.client.HttpServerActor.{CreditCardFailure, CreditCardResponse}
import net.cs.core.objs.CardUser
import net.cs.core.objs.Cards.CreditCard

trait JsonSupport extends SprayJsonSupport {
  import spray.json.DefaultJsonProtocol._

  implicit val cardUser = jsonFormat3(CardUser)

  implicit val creditCard = jsonFormat4(CreditCard)


  implicit val cardResponse = jsonFormat1(CreditCardResponse)

  implicit val cardFailure = jsonFormat1(CreditCardFailure)
}
