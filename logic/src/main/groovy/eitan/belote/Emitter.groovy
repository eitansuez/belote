package eitan.belote

import akka.actor.ActorRef

trait Emitter
{
  ActorRef actorRef

  void emit(String event, args) {
    emit(event, args, Delay.Standard)
  }
  void emit(String event, args, delay) {
    delay = delay ?: Delay.Standard
    actorRef?.tell(new BeloteEvent(name: event, args: args, subsequentDelayMs: delay.delayValue), ActorRef.noSender())
  }
}