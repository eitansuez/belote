package eitan.belote

import groovy.util.logging.Log

@Log
class Round
{
  List<Card> cards
  List<Player> players
  Suit atout

  Player winner
  int points

  static newRound(Round prev, Card card, Player player)
  {
    new Round(cards: prev.cards + card, players: prev.players + player, atout: prev.atout)
  }

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

  private ArrayList<Card> matchingSuit(Suit requested)
  {
    cards.findAll { card -> card.suit == requested }
  }

  ArrayList<Card> atouts()
  {
    cards.findAll { card -> card.suit == atout }
  }

  Card highest(Collection<Card> cardSet)
  {
    cardSet.max { Card card -> card.points(atout) }
  }

  boolean containsAtout()
  {
    cards.any { card -> card.suit == atout }
  }
  boolean requestedAtout()
  {
    requestedSuit() == atout
  }

  private void calculateScore()
  {
    points = cards.inject(0) { int acc, Card card ->
      acc + card.points(atout)
    }
  }

  Suit requestedSuit()
  {
    cards.first().suit
  }

  int size() { cards.size() }
  boolean isEmpty() { cards.empty }

  Card masterCard()
  {
    containsAtout() ?
        highest(atouts()) :
        highest(matchingSuit(requestedSuit()))
  }

  Player master()
  {
    playerOf(masterCard())
  }
}
