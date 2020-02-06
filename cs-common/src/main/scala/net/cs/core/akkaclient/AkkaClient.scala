package net.cs.core.akkaclient

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse, RequestEntity, StatusCode, StatusCodes, Uri, ContentTypes => AkkaClientContentTypes}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.{ClientTransport, Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import net.cs.core.akkaclient.AkkaClient.{GenericError, ServerSideError}
import net.cs.core.api.Request
import net.cs.core.objs.CardTypes.{CSCardType, ScoredCardType}
import net.cs.core.objs.Cards.{CSCard, Card, ScoredCard}
import spray.json._

import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AkkaClient(http: HttpExt, proxy: InetSocketAddress)(implicit materializer: ActorMaterializer, system: ActorSystem, ec: ExecutionContext) {

  import net.cs.core.codec.SprayCodecImplicits._

  private val settings = ConnectionPoolSettings(system).withTransport(ClientTransport.httpsProxy(proxy))

  def getCards(request: Request): Future[Seq[Card]]  = {
    for {
      HttpResponse(status, _, entity, _) <- http.singleRequest(buildRequest(request.endpoint, request.stringifyPayload.get), settings = settings)
      _ <- checkStatus(status, entity)
      data <- loadEntity(entity)
      cards <- parseData(data, request)
    } yield {
      cards
    }
  }

  private def parseData(data: String, request: Request): Future[Seq[Card]] = {

    val cardList = request.cardType match{
      case CSCardType => JsonParser(data).convertTo[Seq[CSCard]]
      case ScoredCardType => JsonParser(data).convertTo[Seq[ScoredCard]]
    }
    cardList match {
      case list: List[Card] => Future.successful(list)
      case _ => Future.failed(new RuntimeException("Could not fetch cards"))

    }
  }
  private def checkStatus(status: StatusCode, entity: HttpEntity)(implicit ec: ExecutionContext): Future[Unit] =
    if (status == StatusCodes.OK) {
      Future.successful(())
    } else {
      val body = loadEntity(entity)
      status match {
        case StatusCodes.InternalServerError => body.flatMap(msg => Future.failed {
          new ServerSideError(s"$status -> $msg")
        })
        case _ => body.flatMap(msg => Future.failed{
          new GenericError(s"$status -> $msg")
        })
      }
    }

  private def loadEntity(entity: HttpEntity)(implicit ec: ExecutionContext): Future[String] =
    for {
      strict <- entity.toStrict(10.seconds)
      bytes <- strict.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield bytes.decodeString("UTF-8")

  private def buildRequest(endpoint: String, payload: String): HttpRequest = {
    val requestPayload: RequestEntity = buildRequestPayload(payload)
    HttpRequest(uri = Uri(endpoint), entity = requestPayload, method = HttpMethods.POST)
  }
  private def buildRequestPayload(payload: String): RequestEntity = {
    HttpEntity(AkkaClientContentTypes.`application/json`, payload)
  }
}

object AkkaClient {

  sealed trait CustomException {
    this: RuntimeException =>
  }

  case class MissingData(message:String) extends RuntimeException(message) with CustomException
  case class ServerSideError(message: String) extends RuntimeException(message) with CustomException
  case class GenericError(message: String) extends RuntimeException(message) with CustomException

  private val cache: MutableMap[String, HttpExt] = MutableMap.empty[String, HttpExt]

  private def getClient(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer): HttpExt = {
    synchronized(cache.getOrElseUpdate("DEFAULT", Http()))
  }

  def getInstance(proxy: InetSocketAddress)(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer): AkkaClient = {
    val client = getClient
    new AkkaClient(client, proxy)
  }
}