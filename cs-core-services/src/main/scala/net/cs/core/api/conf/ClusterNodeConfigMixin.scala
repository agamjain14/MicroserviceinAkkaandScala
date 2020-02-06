package net.cs.core.api.conf


import java.time.Period
import java.time.temporal.TemporalAmount
import java.util

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus.{toFicusConfig, _}


object ClusterNodeConfigMixin {
  def apply(config: Config): Config with ClusterNodeConfigMixin = {
    new AbstractConfigDecorator(config) with ClusterNodeConfigMixin {
      override def getEnum[T <: Enum[T]](enumClass: Class[T], path: String): T = ???

      override def getPeriod(path: String): Period = ???

      override def getTemporal(path: String): TemporalAmount = ???

      override def getEnumList[T <: Enum[T]](enumClass: Class[T], path: String): util.List[T] = ???
    }
  }
}


trait ClusterNodeConfigMixin {
  this: Config =>

  def actorSystemName: String = toFicusConfig(this).as[String]("card.node.actor-system-name")

  def clusterRoles: Set[String] = toFicusConfig(this).as[Set[String]]("akka.cluster.roles")

  def watchedRoles: Set[String] = toFicusConfig(this).as[Set[String]]("card.node.watchedRoles")

}
