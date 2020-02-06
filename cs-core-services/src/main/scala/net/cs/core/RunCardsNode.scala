package net.cs.core

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.stream.ActorMaterializer
import net.cs.core.akkaclient.AkkaClient
import net.cs.core.api.CoreServiceRunner
import net.cs.core.api.conf.ClusterNodeConfigFactory
import net.cs.core.client.HttpServerActor
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

object RunCardsNode {
  def main(args: Array[String]): Unit = {
    val log = LoggerFactory.getLogger(this.getClass)

    val config = ClusterNodeConfigFactory.load()

    implicit val system = ActorSystem(config.actorSystemName, config)
    implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
    implicit val ec : ExecutionContext = system.dispatcher

    val proxy = Some(InetSocketAddress.createUnresolved("localhost", 3128))

    Cluster(system).registerOnMemberUp {
      log.info("node joined the cluster")

      val client = AkkaClient.getInstance(proxy.get)
      val runner = CoreServiceRunner(system)

      system.actorOf(HttpServerActor.props(runner, client, config), "HttpServerActor")
    }

    system.registerOnTermination {
      log.warn("THE ACTOR SYSTEM TERMINATED")
      forceExit(log)
    }
  }

  private def forceExit(log: Logger): Unit = {
    log.warn("The process will be forced to exit")
    System.exit(1)
  }
}
