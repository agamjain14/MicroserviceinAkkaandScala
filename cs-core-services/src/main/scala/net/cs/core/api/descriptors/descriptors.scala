package net.cs.core.api.descriptors

import akka.actor.Props
import com.typesafe.config.Config
import net.cs.core.api.protocol.ResetEntityServiceInstance
import net.cs.core.servicemessages.ServiceMessage


trait ServiceDescriptor[+ServiceConfiguration <: ServiceConfig] {
  protected def ServiceConfigurationCompanion: ServiceConfigCompanion[ServiceConfiguration]

  def name: String

  final def getConfig(globalConfig: Config): ServiceConfiguration = {
    val defaultConfig = globalConfig.getConfig(s"card.core.services.descriptors.defaults")
    val partialConfig = globalConfig.getConfig(s"card.core.services.descriptors.$name").withFallback(defaultConfig)
    ServiceConfigurationCompanion(partialConfig)
  }
}

trait EntityServiceDescriptor extends ServiceDescriptor[EntityServiceConfig] {
  protected override final val ServiceConfigurationCompanion = EntityServiceConfig

  def entityName: String

  final lazy val shardName = s"${entityName}Service"

  def defaultEntityId: String = ""

  protected def idExtractor(c: EntityServiceConfig): PartialFunction[ServiceMessage, String]

  def fullIdExtractor(c: EntityServiceConfig): PartialFunction[ServiceMessage, String] = ({
    case ResetEntityServiceInstance(entityId) => entityId
  }: PartialFunction[ServiceMessage, String]) orElse idExtractor(c)

  def props(c: EntityServiceConfig)(instanceId: String): Props

  def defaultProps(c: EntityServiceConfig): Option[Props] = None

  def shardResolver(c: EntityServiceConfig)(shardingKey: String): Int
}
