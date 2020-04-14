package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Waiter(barista: ActorRef) extends Actor with ActorLogging {

  import Waiter._

  override def receive: Receive = {
    case ServeCoffee(coffee) =>
      barista ! Barista.PrepareCoffee(coffee, sender())
    case Barista.CoffeePrepared(coffee, guest) =>
      guest ! CoffeeServed(coffee)
  }
}

object Waiter {

  case class ServeCoffee(coffee: Coffee)

  case class CoffeeServed(coffee: Coffee)

  def props(barista: ActorRef): Props = Props(new Waiter(barista))

}
