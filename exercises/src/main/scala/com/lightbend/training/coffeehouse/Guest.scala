package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}

import scala.concurrent.duration.FiniteDuration

class Guest(waiter: ActorRef,
            favouriteCoffee: Coffee,
            finishCoffeeDuration: FiniteDuration,
            caffeineLimit: Int)
    extends Actor
    with ActorLogging
    with Timers {

  import Guest._

  private var coffeeCount: Int = 0

  orderCoffee()

  override def postStop(): Unit = {
    log.info("Goodbye")
    super.postStop()

  }

  override def receive: Receive = {
    case Waiter.CoffeeServed(`favouriteCoffee`) =>
      coffeeCount += 1
      log.info(s"Enjoying my $coffeeCount $favouriteCoffee")
      timers.startSingleTimer(
        "coffee-finished",
        CoffeeFinished,
        finishCoffeeDuration
      )
    case Waiter.CoffeeServed(otherCoffee) =>
      log.info(s"Expected a $favouriteCoffee, but got a $otherCoffee")
      waiter ! Waiter.Complaint(favouriteCoffee)
    case CoffeeFinished if coffeeCount > caffeineLimit =>
      throw CaffeineException
    case CoffeeFinished => orderCoffee()
  }

  private def orderCoffee(): Unit = {
    waiter ! Waiter.ServeCoffee(favouriteCoffee)
  }
}

object Guest {

  case object CoffeeFinished
  case object CaffeineException extends IllegalStateException

  def props(waiter: ActorRef,
            favCoffee: Coffee,
            finishCoffeeDuration: FiniteDuration,
            caffeineLimit: Int): Props =
    Props(new Guest(waiter, favCoffee, finishCoffeeDuration, caffeineLimit))
}
