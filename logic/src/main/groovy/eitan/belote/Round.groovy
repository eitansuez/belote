package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Round implements Emitter
{
  List<Card> cards = []
  List<Player> players = []
  Game game

  Player winner
  int points

  Round nextRound(Card card, Player player)
  {
    def round = new Round(cards: this.cards + card,
        players: this.players + player,
        game: this.game,
        actorRef: this.actorRef)

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
    game.roundDone(this)
    emit("roundEnds", [winner, points])
  }

  ArrayList<Card> atouts()
  {
    matchingSuit(game.atout)
  }

  private ArrayList<Card> matchingSuit(Suit suit)
  {
    cards.findAll { card -> card.suit == suit }
  }

  Card highest(Collection<Card> cardSet)
  {
    cardSet.max { Card card -> card.points(game.atout) }
  }

  boolean containsAtout()
  {
    cards.any { card -> card.suit == game.atout }
  }
  boolean requestedAtout()
  {
    requestedSuit() == game.atout
  }

  private void calculateScore()
  {
    points = cards.inject(0) { int acc, Card card ->
      acc + card.points(game.atout)
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

  private Player playerOf(Card winningCard)
  {
    players[cards.indexOf(winningCard)]
  }

}
