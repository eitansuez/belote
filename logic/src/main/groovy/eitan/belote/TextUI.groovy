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
  void playCard(Player player, Card card)
  {
    log.info("${player} plays ${card}")
  }

  @Override
  Card chooseCard(Player player, Set<Card> validCards)
  {
    return null
  }

  @Override
  void clearHand(Player player)
  {
    log.info("${player} hand is cleared")
  }

  @Override
  boolean envoi(Player player, Suit candidateSuit)
  {
    return false
  }

  @Override
  Suit envoi(Player player)
  {
    return null
  }

  @Override
  void roundEnds(Player winner, int points)
  {
    log.info(">>${winner} wins hand (${points} points)\n")
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
