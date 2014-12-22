package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class TextUI implements UI
{
  @Override
  void turnUpCard(Card card)
  {
    log.info("dealer turns over candidate card ${card}")
  }

  @Override
  void receiveCard(Player player, Card card)
  {
    log.info("${player} receives ${card}")
  }

  @Override
  void gameForfeit()
  {
    log.info "no one envoi'd, game done"
  }

  @Override
  void playCard(Player player, Card card)
  {
    log.info("${player} plays ${card}")
  }

  @Override
  void roundEnds(Player winner, int points)
  {
    log.info(">>${winner} wins hand (${points} points)\n")
  }

  @Override
  void playerDecision(Player player, boolean envoi, Suit suit)
  {
    log.info("${player} makes a decision on suit ${suit} : " + (envoi ? "go!" : "pass"))
  }

  @Override
  void gameUpdate(Team team1, int score1, Team team2, int score2)
  {
    log.info("${team1}: ${score1} / ${team2}: ${score2}")
  }

  @Override
  void gameEnds(Team team1, int score1, Team team2, int score2)
  {
    log.info("")
  }
}
