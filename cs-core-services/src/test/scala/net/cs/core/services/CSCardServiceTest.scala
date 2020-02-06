package net.cs.core.services

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.actor.Status.Failure
import akka.stream.ActorMaterializer
import akka.testkit.{TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import net.cs.core.akkaclient.AkkaClient
import net.cs.core.akkaclient.AkkaClient.GenericError
import net.cs.core.api.descriptors.EntityServiceConfig
import net.cs.core.objs.CardUser
import net.cs.core.objs.Cards.CreditCard
import net.cs.core.servicemessages.CSCardServiceMessage
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FunSpecLike, Matchers}

import scala.concurrent.duration.Duration

class CSCardServiceTest extends TestKit(ActorSystem("CSCardServiceTest", ConfigFactory.load("test.conf")))
   with FunSpecLike with Matchers with BeforeAndAfter with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)

  val entityId = "124"
  val proxy = Some(InetSocketAddress.createUnresolved("localhost", 3128))
  val client = AkkaClient.getInstance(proxy.get)

  var cards = List(CreditCard("CSCards","SuperSaver Card",21.4,0.138), CreditCard("CSCards","SuperSpender Card",19.2,0.136))

  val user = CardUser("John Smith", 500, 28000)

  val user1 = CardUser("John Smith", 5001, 28000)

  val dummyServiceConfig = {
    import scala.concurrent.duration._
    new EntityServiceConfig(role = "test-role", nbShards = 2, entityName = "test-entity", maxIdleDuration = 10.second, maxInitDuration = 10.second)
  }

  describe("CSCardsService should retrieve CreditCards by CardUser") {
    it("Should retrieve all the CSCards for given User") {

      val mySender = TestProbe()
      val csCardService = system.actorOf(CSCardService.props(dummyServiceConfig)(entityId))

      mySender.send(csCardService, CSCardServiceMessage.GetCSCards(entityId, user, client))

      mySender.expectMsg(Duration.apply(10, TimeUnit.SECONDS), cards)
      system.stop(csCardService)

    }
  }

  describe("CSCardsService should throw generic error on incorrect input") {
    it("Should not retrieve any CSCard for given User") {

      val mySender = TestProbe()
      val csCardService = system.actorOf(CSCardService.props(dummyServiceConfig)(entityId))

      mySender.send(csCardService, CSCardServiceMessage.GetCSCards(entityId, user1, client))

      mySender.expectMsg(Duration.apply(10, TimeUnit.SECONDS), Failure(GenericError("400 Bad Request -> requirement failed: creditScore must be between 0 and 700 (inclusive)")))
      system.stop(csCardService)

    }
  }
}
