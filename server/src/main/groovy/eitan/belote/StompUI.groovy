package eitan.belote

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class StompUI implements UI
{
  @Autowired
  private final SimpMessagingTemplate template;

  private void sendCmd(cmd, args) {
    template.convertAndSend('/topic/belote', [cmd: cmd, args: args.collect { arg -> marshal(arg) }])
  }

  private static marshal(arg) {
    if (arg instanceof Player)
    {
      ((Player) arg).name
    }
    else if (arg instanceof Card) {
      ((Card) arg).toString()
    }
    else if (arg instanceof Enum) {
      ((Enum) arg).name()
    }
    else {
      arg
    }
  }

  @Override
  void turnUpCard(Card card)
  {
    sendCmd('turnUpCard', [card])
  }

  @Override
  void receiveCard(Player player, Card card)
  {
    sendCmd('receiveCard', [player, card])
  }

  @Override
  void playerDecision(Player player, boolean envoi, Suit suit)
  {
    sendCmd('playerDecision', [player, envoi, suit])
  }

  @Override
  void playCard(Player player, Card card)
  {

  }

  @Override
  void clearHand(Player player)
  {

  }

  @Override
  void roundEnds(Player winner, int points)
  {

  }

  @Override
  void gameUpdate(Team team1, int score1, Team team2, int score2)
  {

  }

  @Override
  void gameEnds(Team team1, int score1, Team team2, int score2)
  {

  }
}
