package eitan.belote

class Round
{
  List<Card> cards
  List<Player> players
  Suite atout

  Player winner
  int points

  void resolve()
  {
    boolean containsAtout = cards.any { card -> card.suite == atout }

    points = cards.inject(0) { int acc, Card card ->
      acc + card.points(atout)
    }

    Suite requested = cards.first().suite

    Card winningCard = (containsAtout) ?
      cards.findAll { card -> card.suite == atout }.max { Card card -> card.points(atout) } :
      cards.findAll { card -> card.suite == requested }.max { Card card -> card.points() }

    winner = players[cards.indexOf(winningCard)]
  }

}
