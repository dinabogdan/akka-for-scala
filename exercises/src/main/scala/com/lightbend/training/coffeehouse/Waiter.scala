package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, Props}

class Waiter extends Actor with ActorLogging {

  import Waiter._

  override def receive: Receive = {
    case ServeCoffee(coffee) => sender() ! CoffeeServed(coffee)
  }
}

object Waiter {

  case class ServeCoffee(coffee: Coffee)

  case class CoffeeServed(coffee: Coffee)

  def props: Props = Props(new Waiter)

}
