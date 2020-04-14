package com.lightbend.training.coffeehouse

import java.util.concurrent.TimeUnit

import akka.actor.{
  Actor,
  ActorLogging,
  ActorRef,
  OneForOneStrategy,
  Props,
  SupervisorStrategy,
  Terminated
}

import scala.concurrent.duration._

class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging {

  import CoffeeHouse._

  log.debug("CoffeeHouse Open")

  override def supervisorStrategy: SupervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case Guest.CaffeineException => SupervisorStrategy.Stop
    }
    OneForOneStrategy()(decider.orElse(super.supervisorStrategy.decider))
  }

  private var guestBook: Map[ActorRef, Int] = Map.empty.withDefaultValue(0)

  private val finishCoffeeDuration: FiniteDuration =
    context.system.settings.config
      .getDuration(
        "coffee-house.guest.finish-coffee-duration",
        TimeUnit.MILLISECONDS
      )
      .millis

  private val prepareCoffeeDuration: FiniteDuration =
    context.system.settings.config
      .getDuration(
        "coffee-house.barista.prepare-coffee-duration",
        TimeUnit.MILLISECONDS
      )
      .millis

  private val barista = createBarista()
  private val waiter = createWaiter()

  override def receive: Receive = {
    case CreateGuest(favCoffee, caffeineLimit) =>
      val guest = createGuest(favCoffee, caffeineLimit)
      guestBook += guest -> 0
      log.info(s"Guest $guest added to guest book")
      context.watch(guest)
    case ApproveCoffee(coffee, guest) if guestBook(guest) < caffeineLimit =>
      guestBook += guest -> (guestBook(guest) + 1)
      log.info(s"Guest $guest caffeine count incremented.")
      barista forward Barista.PrepareCoffee(coffee, guest)
    case ApproveCoffee(coffee, guest) =>
      log.info(s"Sorry, $guest, but you have reached your limit.")
      context.stop(guest)
    case Terminated(guest) =>
      log.info(s"Thanks, $guest, for being our guest!")
      guestBook -= guest
    case _ => sender() ! "Coffee brewing"
  }

  protected def createGuest(favCoffee: Coffee,
                            guestCaffeineLimit: Int): ActorRef =
    context.actorOf(
      Guest.props(waiter, favCoffee, finishCoffeeDuration, guestCaffeineLimit)
    )

  protected def createBarista(): ActorRef = {
    context.actorOf(Barista.props(prepareCoffeeDuration), "barista")
  }

  protected def createWaiter(): ActorRef =
    context.actorOf(Waiter.props(self), "waiter")
}

object CoffeeHouse {

  case class ApproveCoffee(coffee: Coffee, guest: ActorRef)
  case class CreateGuest(favCoffee: Coffee, caffeineLimit: Int)

  def props(caffeineLimit: Int): Props = Props(new CoffeeHouse(caffeineLimit))
}
