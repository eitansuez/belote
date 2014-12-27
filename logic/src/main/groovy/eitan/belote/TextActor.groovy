package eitan.belote

import akka.actor.UntypedActor
import groovy.util.logging.Slf4j


@Slf4j
class TextActor extends UntypedActor
{
  @Override
  void onReceive(Object message) throws Exception
  {
    if (message instanceof BeloteEvent)
    {
      BeloteEvent event = (BeloteEvent) message
      this.invokeMethod(event.name, event.args ?: null)
    }
  }

  void turnUpCard(Card card)
  {
    log.info("dealer turns over candidate card ${card}")
  }

  void receiveCard(Player player, Card card)
  {
    log.info("${player} receives ${card}")
  }

  void playCard(Player player, Card card)
  {
    log.info("${player} plays ${card}")
  }

  void roundEnds(Player winner, int points)
  {
    log.info(">>${winner} wins hand (${points} points)\n")
  }

  void playerDecision(Player player, boolean envoi, Suit suit)
  {
    log.info("${player} makes a decision on suit ${suit} : " + (envoi ? "go!" : "pass"))
  }

  void gameUpdate(Team team1, int score1, Team team2, int score2)
  {
    log.info("${team1}: ${score1} / ${team2}: ${score2}")
  }

  void gameEnds(Team winningTeam)
  {
    if (winningTeam)
    {
      log.info "game ends, ${winningTeam} wins"
    }
    else
    {
      log.info "game ends"
    }
  }

}