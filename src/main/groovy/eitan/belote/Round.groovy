package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Round
{
  List<Card> cards = []
  List<Player> players = []
  Suit atout

  Player winner
  int points

  Round newRound(Card card, Player player)
  {
    def round = new Round(cards: this.cards + card, players: this.players + player, atout: this.atout)
    // TODO:  need an afterCreate
    if (round.isComplete()) {
      round.resolve()
    }
    round
  }

  boolean isComplete() {
    size() == 4
  }

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

  ArrayList<Card> matchingSuit(Suit suit)
  {
    cards.findAll { card -> card.suit == suit }
  }

  ArrayList<Card> atouts()
  {
    matchingSuit(atout)
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
