package net.cs.core.api

import scala.reflect.runtime.universe
import com.typesafe.config.Config
import net.cs.core.api.descriptors.{EntityServiceDescriptor, ServiceConfig, ServiceDescriptor}

import scala.util.{Failure, Success, Try}


object CoreServiceRegistry {

  final val CSCardService = "cs-card-service"
  final val ScoredCardService = "scored-card-service"

  protected final val servicesNames = Seq(CSCardService, ScoredCardService)

  def apply(config: Config) = {
    new CoreServiceRegistry(config)
  }
}

class CoreServiceRegistry(config: Config) {

  private def loadDescriptors(): Seq[ServiceDescriptor[_ <: ServiceConfig]] = {
    val classLoaders = getClass.getClassLoader
    val rootMirror = universe.runtimeMirror(classLoaders)
    for {
      serviceName <- CoreServiceRegistry.servicesNames
    } yield Try {
      val serviceClassName = config.getString(s"card.core.services.descriptors.$serviceName.class")
      val clazz = Class.forName(serviceClassName, true, classLoaders)
      val classSymbol = rootMirror.classSymbol(clazz)

      val moduleSymbol = if (classSymbol.isModule) {
        classSymbol.asModule
      } else {
        classSymbol.companion.asModule
      }

      val moduleMirror = rootMirror.reflectModule(moduleSymbol)
      moduleMirror.instance.asInstanceOf[ServiceDescriptor[_ <: ServiceConfig]]
    } match {
      case Success(value) => value
      case Failure(t) => throw new IllegalArgumentException(s"Could not load the descriptor for service $serviceName", t)
    }
  }

  val services = loadDescriptors()


  def entityServiceDescriptors: Seq[EntityServiceDescriptor] = services.collect {
    case s: EntityServiceDescriptor => s
  }
}