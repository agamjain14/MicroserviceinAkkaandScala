package net.cs.core.api.conf

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

import org.apache.commons.lang3.text.StrTokenizer

object ClusterNodeConfigFactory {

  def load(): Config with ClusterNodeConfigMixin = {
    val config = this.transform(ConfigFactory.load())
    ClusterNodeConfigMixin(config)
  }


  def load(location: String): Config with ClusterNodeConfigMixin = {
    val config = this.transform(ConfigFactory.load(location))
    ClusterNodeConfigMixin(config)
  }


  def transform(config: Config): Config = transformationChain(config)

  private val transformationChain = Function.chain(Seq(this.addRoles _, this.addSeedNodes _))

  private def scanStringList(str: String): Seq[String] = {
    val tokenizer = new StrTokenizer(str.trim, ',', '"')
    tokenizer.getTokenArray.map(_.trim)
  }

  private def addRoles(config: Config) = {
    val values = config.as[Option[String]]("card.node.roles").toSeq.flatMap(scanStringList)
    if (values.nonEmpty) {
      ConfigFactory.parseString(
        s"""
akka.cluster.roles = ${values.mkString("[\"", "\", \"", "\"]")}
"""
      ).withFallback(config)
    } else {
      config
    }
  }

  private def addSeedNodes(config: Config) = {
    val values = config.as[Option[String]]("card.node.seed-nodes").toSeq.flatMap(scanStringList)
    if (values.nonEmpty) {
      ConfigFactory.parseString(
        s"""
akka.cluster.seed-nodes = ${values.mkString("[\"", "\", \"", "\"]")}
"""
      ).withFallback(config)
    } else {
      config
    }
  }
}
