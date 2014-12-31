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
    log.info("game score: ${team1}: ${score1} / ${team2}: ${score2}")
  }

  void gameStarts(int gameNumber)
  {
    log.info("Game #${gameNumber} about to begin..")
  }

  void gameEnds(Team winningTeam, Team team1, int score1, Team team2, int score2)
  {
    if (winningTeam)
    {
      log.info "game ends, ${winningTeam} wins; final score: ${team1}: ${score1} / ${team2}: ${score2}"
    }
    else
    {
      log.info "game ends"
    }
  }

  void partieStarts(Team team1, Team team2, players)
  {
    log.info("La partie commence avec teams: ${team1} vs ${team2}")
  }

  void partieUpdate(Team team1, int score1, Team team2, int score2)
  {
    log.info("partie score: ${team1}: ${score1} / ${team2}: ${score2}")
  }

  void partieEnds(Team winner)
  {
    log.info("partie is over;  team ${winner} has won")
  }

}
