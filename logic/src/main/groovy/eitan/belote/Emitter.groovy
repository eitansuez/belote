package eitan.belote

import akka.actor.ActorRef

trait Emitter
{
  ActorRef actorRef

  void emit(String event, args) {
    actorRef?.tell(new BeloteEvent(name: event, args: args), ActorRef.noSender())
  }
}