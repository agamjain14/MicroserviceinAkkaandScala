package net.cs.core.api.protocol

import net.cs.core.servicemessages.ServiceMessage

case class ResetEntityServiceInstance(entityId: String) extends ServiceMessage
