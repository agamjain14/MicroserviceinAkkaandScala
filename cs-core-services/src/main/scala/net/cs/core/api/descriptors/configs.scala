package net.cs.core.api.descriptors

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration

sealed trait ServiceConfig {
  protected def config: Config

  val role = config.as[String]("role")

  val dispatcherString = config.as[String]("dispatcher")


  final val serviceConfig = config
}

sealed trait ServiceConfigCompanion[+T <: ServiceConfig] {
  def apply(config: Config): T
}

object EntityServiceConfig extends ServiceConfigCompanion[EntityServiceConfig] {
  def apply(config: Config) = new EntityServiceConfig(config)
}

class EntityServiceConfig(override protected val config: Config) extends ServiceConfig {
  require(config.as[String]("type") == "entity-service")

  /**
    * Constructor to be used only for unit testing.
    */
  private[core] def this(role: String, nbShards: Int, entityName: String, maxIdleDuration: FiniteDuration, maxInitDuration: FiniteDuration, additionalConfig: Config = ConfigFactory.empty()) = {
    this(ConfigFactory.parseString(
      s"""
type = "entity-service"
role = "$role"
nb-shards = $nbShards
entity-name = "$entityName"
dispatcher = ""
max-idle-time = $maxIdleDuration
max-init-time = $maxInitDuration
config = ${additionalConfig.root().render()}
"""
    ))
  }


  val nbShards = config.as[Int]("nb-shards")
}