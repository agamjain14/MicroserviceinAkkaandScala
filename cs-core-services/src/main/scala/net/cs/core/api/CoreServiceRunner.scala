package net.cs.core.api

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.event.Logging
import net.cs.core.api.actors.CardsServiceProxyActor
import net.cs.core.api.conf.ClusterNodeConfigMixin
import net.cs.core.api.descriptors.{ServiceConfig, ServiceDescriptor}
import net.cs.core.servicemessages.ServiceMessage

object CoreServiceRunner {
  def apply(system: ActorSystem) = {
    new CoreServiceRunner(system)
  }
}

class CoreServiceRunner(system: ActorSystem) {

  private val log = Logging.getLogger(system, this)
  private val (serviceConfigMap, serviceEndpointsMap) = init()

  private def init()= {
    val registry = CoreServiceRegistry(system.settings.config)

    val roles = ClusterNodeConfigMixin(system.settings.config).clusterRoles
    val shardedServicesSeq = for {
      serviceDescriptor <- registry.entityServiceDescriptors
      serviceConfig = serviceDescriptor.getConfig(system.settings.config)
    } yield {
      val proxyMode = !roles.contains(serviceConfig.role)
      val settings: ClusterShardingSettings = ClusterShardingSettings(system)
        .withRole(Some(serviceConfig.role))
        .withRememberEntities(false)
      val extractEntityId = Function.unlift { x: Any =>
        for {
          m <- Some(x).collect({ case m: ServiceMessage => m })
          id <- serviceDescriptor.fullIdExtractor(serviceConfig).lift(m)
          shard = serviceDescriptor.shardResolver(serviceConfig)(id)
        } yield (shard.toString, m)
      }
      val extractShardId = Function.unlift { x: Any =>
        for {
          m <- Some(x).collect({ case m: ServiceMessage => m })
          id <- serviceDescriptor.fullIdExtractor(serviceConfig).lift(m)
        } yield serviceDescriptor.shardResolver(serviceConfig)(id).toString
      }
      val shardRegion = if (proxyMode) {
        ClusterSharding(system).startProxy(
          typeName = serviceDescriptor.shardName,
          role = Some(serviceConfig.role),
          extractEntityId = extractEntityId,
          extractShardId = extractShardId,
        )
      } else {
        ClusterSharding(system).start(
          typeName = serviceDescriptor.shardName,
          entityProps = CardsServiceProxyActor.props(serviceDescriptor, serviceConfig).withDispatcher(serviceConfig.dispatcherString),
          settings = settings,
          extractEntityId = extractEntityId,
          extractShardId = extractShardId
        )
      }

      log.info("Started entity service {} (proxy = {}) at: {}", serviceDescriptor.name, proxyMode, shardRegion.path)
      (serviceDescriptor.name, serviceConfig, shardRegion)
    }


    val allServices = shardedServicesSeq
    val configMap = allServices.map({ case (n, c, _) => (n, c) }).toMap[String, ServiceConfig]
    val endpointsMap = allServices.map({ case (n, _, ref) => (n, ref) }).toMap[String, ActorRef]


    (configMap, endpointsMap)
  }


  def service[U <: ServiceConfig](descriptor: ServiceDescriptor[U]): ActorRef = serviceEndpointsMap.getOrElse(descriptor.name, throw new IllegalArgumentException(s"Cannot get an appropriate entrypoint for service ${descriptor.name}"))


  def service(serviceName: String): ActorRef = serviceEndpointsMap.getOrElse(serviceName, throw new IllegalArgumentException(s"Cannot get an appropriate entrypoint for service $serviceName"))

}