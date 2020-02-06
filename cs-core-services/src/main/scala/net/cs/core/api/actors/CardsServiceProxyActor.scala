package net.cs.core.api.actors
import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.dispatch.MessageDispatcher
import akka.util.Timeout
import net.cs.core.api.descriptors.{EntityServiceConfig, EntityServiceDescriptor}
import net.cs.core.objs.Cards.CreditCard
import net.cs.core.servicemessages.ServiceMessage

import scala.concurrent.duration._

object CardsServiceProxyActor {
  case object Passivate

  def props(serviceDescriptor: EntityServiceDescriptor, c: EntityServiceConfig) = {
    Props(classOf[CardsServiceProxyActor], serviceDescriptor, c)
  }
}

class CardsServiceProxyActor(serviceDescriptor: EntityServiceDescriptor, c: EntityServiceConfig) extends Actor
  with ActorLogging{

  val actorsService = scala.collection.mutable.Map.empty[String, ActorRef]

  var creditCardsList : Seq[CreditCard] = Seq.empty

  implicit val timeout = Timeout(10 seconds)

  override def preStart() = {
    super.preStart()
    log.debug("New entry created")
  }

  override def postStop() = {
    log.debug("Entry stopped")
    super.postStop()
  }

  def receive: Receive = {

    case msg: ServiceMessage =>
      classify.lift(msg) match {
        case Some(id) if !id.equals(serviceDescriptor.defaultEntityId) =>
          val myEntityInstanceActor = actorsService.getOrElseUpdate(id, {
            val ref = createInstance(id)
            context.watch(ref) // Monitor the actor
            log.debug("New entity actor created: (id = [{}], ref = [{}])", id, ref)
            ref
          })

          //myEntityInstanceActor ! msg
          myEntityInstanceActor forward msg


        case _ =>
          (actorsService.get(serviceDescriptor.defaultEntityId), serviceDescriptor.defaultProps(c)) match {
            case (Some(ref), _) =>
              ref.forward(msg)

            case (None, Some(props)) =>
              val ref = context.actorOf(props.withDispatcher(currentDispatcherId))
              context.watch(ref) // Monitor the actor
              log.debug("Default handler actor created: (ref = [{}])", ref)
              actorsService.put(serviceDescriptor.defaultEntityId, ref)
              ref.forward(msg)

            case (None, None) =>
              log.warning("Message received but cannot extract an id from it, it will be dropped: {}", msg)
              this.unhandled(msg)
          }
      }

    case CardsServiceProxyActor.Passivate =>
      this.passivate(sender()).foreach { entityId =>
        sender ! PoisonPill
        log.debug("PoisonPill sent to child actor: (id = [{}], ref = [{}])", entityId, sender())
      }

    case Terminated(ref) =>
      this.passivate(ref).foreach { entityId =>
        log.debug("Child actor terminated: (id = [{}], ref = [{}])", entityId, ref)
      }
  }

  // -------------- Utils ------------------------------------
  def passivate(ref: ActorRef) = {
    val keys = actorsService.collectFirst {
      case (key, `ref`) => key
    }
    keys.foreach { id =>
      log.debug("Entity actor removed from the list: (id = [{}], ref = [{}])", id, ref)
      actorsService -= id
    }
    keys
  }

  def classify: PartialFunction[ServiceMessage, String] = serviceDescriptor.fullIdExtractor(c)


  def createInstance(entityId: String) = {
    context.actorOf(serviceDescriptor.props(c)(entityId).withDispatcher(currentDispatcherId))
  }

  def currentDispatcherId(implicit context: ActorContext): String = {
    context.dispatcher match {
      case messageDispatcher: MessageDispatcher => messageDispatcher.id
      case dispatcher =>
        log.warning("No explicit MessageDispatcher found in the context, falling back to the default dispatcher")
        context.system.dispatchers.defaultGlobalDispatcher.id
    }
  }

}
