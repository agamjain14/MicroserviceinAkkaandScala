package net.cs.core.api.actors

import akka.actor.{Actor, ActorLogging}
import net.cs.core.api.descriptors.{EntityServiceConfig, EntityServiceDescriptor}

trait EntityServiceActor extends Actor with ActorLogging {

  def serviceDescriptor: EntityServiceDescriptor

  def serviceConfig: EntityServiceConfig

  def entityId: String

  private def purgingMailbox: Receive = {
    case m =>
      log.debug("Message forwarded back to parent: {}", m)
      context.parent.forward(m)
  }

  def passivateSelf() = {
    context.parent ! CardsServiceProxyActor.Passivate
    log.debug("Purging mailbox...")
    context.become(purgingMailbox)
  }
}
