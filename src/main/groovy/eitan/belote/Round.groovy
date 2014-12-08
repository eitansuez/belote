package eitan.belote

class Round
{
  Map<Card, Player> cards
  Suite atout

  Player winner
  int points

  void resolve()
  {
    def cardSet = cards.keySet()
    boolean containsAtout = cardSet.any { card -> card.suite == atout }

    points = cardSet.inject(0) { int acc, Card card ->
      acc + card.points(atout)
    }

    Suite asked = cards.entrySet().iterator().next().key.suite

    Card winningCard = (containsAtout) ?
      cardSet.findAll { card -> card.suite == atout }.max { Card card -> card.points(atout) } :
      cardSet.findAll { card -> card.suite == asked }.max { Card card -> card.points() }

    winner = cards[winningCard]
  }

}
