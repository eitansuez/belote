package eitan.belote

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class RemoteStrategy implements Strategy
{
  Player player

  @Autowired
  private final SimpMessagingTemplate template

  private void sendCmd(cmd, args) {
    template.convertAndSend('/topic/belote', [cmd: cmd, args: args.collect { arg -> StompActor.marshal(arg) }])
  }

  @Override
  void offer(Game game, Card candidate)
  {
    sendCmd('offer', [player, candidate])
  }

  @Override
  void offer(Game game)
  {
    sendCmd('offer', [player])
  }

  @Override
  void play(Game game, Set<Card> validCards, Round round)
  {
    sendCmd('play', [player, validCards])
  }
}
