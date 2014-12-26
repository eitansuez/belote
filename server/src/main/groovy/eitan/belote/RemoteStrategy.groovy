package eitan.belote

import akka.actor.ActorRef
import org.springframework.stereotype.Component

@Component
class RemoteStrategy implements Strategy
{
  Player player
  ActorRef actorRef

  @Override
  void offer(Game game, Card candidate)
  {
    actorRef.tell(new BeloteEvent(name: 'offer', args: [player, candidate.suit]), ActorRef.noSender())
  }

  @Override
  void offer(Game game)
  {
    actorRef.tell(new BeloteEvent(name: 'offer', args: [player]), ActorRef.noSender())
  }

  @Override
  void play(Game game, Set<Card> validCards, Round round)
  {
    actorRef.tell(new BeloteEvent(name: 'play', args: [player, validCards]), ActorRef.noSender())
  }
}
