package eitan.belote

import groovy.util.logging.Log

@Log
class Round
{
  List<Card> cards
  List<Player> players
  Suite atout

  Player winner
  int points

  // TODO: how to assert cards.size == players.size on construction?

  void resolve()
  {
    calculateScore()
    winner = master()
    log.info(">>${winner} wins hand (${points} points)\n")
  }

  private Player playerOf(Card winningCard)
  {
    players[cards.indexOf(winningCard)]
  }

  private ArrayList<Card> matchingSuite(Suite requested)
  {
    cards.findAll { card -> card.suite == requested }
  }

  ArrayList<Card> atouts()
  {
    cards.findAll { card -> card.suite == atout }
  }

  Card highest(Collection<Card> cardSet)
  {
    cardSet.max { Card card -> card.points(atout) }
  }

  boolean containsAtout()
  {
    cards.any { card -> card.suite == atout }
  }
  boolean requestedAtout()
  {
    requestedSuite() == atout
  }

  private void calculateScore()
  {
    points = cards.inject(0) { int acc, Card card ->
      acc + card.points(atout)
    }
  }

  Suite requestedSuite()
  {
    cards.first().suite
  }

  int size() { cards.size() }
  boolean isEmpty() { cards.empty }

  Card masterCard()
  {
    containsAtout() ?
        highest(atouts()) :
        highest(matchingSuite(requestedSuite()))
  }

  Player master()
  {
    playerOf(masterCard())
  }
}
