package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}

import scala.concurrent.duration.FiniteDuration

class Guest(waiter: ActorRef,
            favouriteCoffee: Coffee,
            finishCoffeeDuration: FiniteDuration)
    extends Actor
    with ActorLogging
    with Timers {

  import Guest._

  private var coffeeCount: Int = 0

  orderCoffee()

  override def receive: Receive = {
    case Waiter.CoffeeServed(coffee) =>
      coffeeCount += 1
      log.info(s"Enjoying my $coffeeCount $coffee")
      timers.startSingleTimer(
        "coffee-finished",
        CoffeeFinished,
        finishCoffeeDuration
      )
    case CoffeeFinished => orderCoffee()
  }

  private def orderCoffee(): Unit = {
    waiter ! Waiter.ServeCoffee(favouriteCoffee)
  }
}

object Guest {

  case object CoffeeFinished

  def props(waiter: ActorRef,
            favCoffee: Coffee,
            finishCoffeeDuration: FiniteDuration): Props =
    Props(new Guest(waiter, favCoffee, finishCoffeeDuration))
}
