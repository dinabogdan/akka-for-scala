package com.lightbend.training.coffeehouse

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration._

class CoffeeHouse extends Actor with ActorLogging {

  import CoffeeHouse._

  log.debug("CoffeeHouse Open")

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
    case CreateGuest(favCoffee) => createGuest(favCoffee)
    case _                      => sender() ! "Coffee brewing"
  }

  protected def createGuest(favCoffee: Coffee): ActorRef =
    context.actorOf(Guest.props(waiter, favCoffee, finishCoffeeDuration))

  protected def createBarista(): ActorRef = {
    context.actorOf(Barista.props(prepareCoffeeDuration), "barista")
  }

  protected def createWaiter(): ActorRef =
    context.actorOf(Waiter.props(barista), "waiter")
}

object CoffeeHouse {

  case class CreateGuest(favCoffee: Coffee)

  def props: Props = Props(new CoffeeHouse)
}
