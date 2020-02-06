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
import net.cs.core.servicemessages.ScoredCardServiceMessage
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FunSpecLike, Matchers}

import scala.concurrent.duration.Duration

class ScoredCardServiceTest extends TestKit(ActorSystem("ScoredCardServiceTest", ConfigFactory.load("test.conf")))
  with FunSpecLike with Matchers with BeforeAndAfter with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)

  val entityId = "124"
  val proxy = Some(InetSocketAddress.createUnresolved("localhost", 3128))
  val client = AkkaClient.getInstance(proxy.get)

  var cards = List(CreditCard("ScoredCards","ScoredCard Builder",19.4,0.213))

  val user = CardUser("John Smith", 500, 28000)

  val user2 = CardUser("John Smith", 5001, 28000)

  val dummyServiceConfig = {
    import scala.concurrent.duration._
    new EntityServiceConfig(role = "test-role", nbShards = 2, entityName = "test-entity", maxIdleDuration = 10.second, maxInitDuration = 10.second)
  }

  describe("ScoredCardsService should retrieve CreditCards by CardUser") {
    it("Should retrieve all the ScoredCards for given User") {

      val mySender1 = TestProbe()
      val scoredCardService = system.actorOf(ScoredCardService.props(dummyServiceConfig)(entityId))

      mySender1.send(scoredCardService, ScoredCardServiceMessage.GetScoredCards(entityId, user, client))

      mySender1.expectMsg(Duration.apply(10, TimeUnit.SECONDS), cards)
      system.stop(scoredCardService)

    }
  }

  describe("ScoredCardsService should throw generic error on incorrect input") {
    it("Should not retrieve any ScoredCard for given User") {

      val mySender2 = TestProbe()
      val scoredCardService = system.actorOf(ScoredCardService.props(dummyServiceConfig)(entityId))

      mySender2.send(scoredCardService, ScoredCardServiceMessage.GetScoredCards(entityId, user2, client))

      mySender2.expectMsg(Duration.apply(15, TimeUnit.SECONDS), Failure(GenericError("requirement failed: score must be between 0 and 700 (inclusive)")))
      system.stop(scoredCardService)

    }
  }
}
