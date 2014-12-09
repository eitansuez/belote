package eitan.belote

class Player
{
  String name
  Set<Card> hand = [] as HashSet<Card>
  Team team

  def dealCard(Card card)
  {
    hand << card
  }

  def dealCards(List<Card> cards)
  {
    cards.each { card ->
      dealCard(card)
    }
  }

  @Override
  String toString() {
    "Player: $name"
  }

  def playCard(Card card)
  {
    if (hand.remove(card)) {
      return card
    }
    throw new NoSuchElementException("Player doesn't have card ${card} to play!")
  }

  Set<Card> validCards(List<Card> placed, Suite atout)
  {
    if (placed.empty)
    {
      return hand
    }

    Suite requested = placed.first().suite

    if (requested == atout)
    {
      Set<Card> higherAtouts = findHigherAtouts(placed, atout)
      if (higherAtouts) {
        return higherAtouts
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suite == atout
      }
      if (allAtouts) {
        return allAtouts
      }
      return hand
    }

    Set<Card> matchingSuite = hand.findAll { card -> card.suite == requested }
    if (matchingSuite) {
      return matchingSuite
    }

    if (haveAtout(atout))
    {
      if (placed.find { card -> card.suite == atout }) {
        Set<Card> higherAtouts = findHigherAtouts(placed, atout)
        if (higherAtouts) {
          return higherAtouts
        }
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suite == atout
      }
      if (allAtouts) {
        return allAtouts
      }

    }

    hand
  }

  private Set<Card> findHigherAtouts(List<Card> placed, Suite atout)
  {
    Card highestAtout = placed.findAll { card -> card.suite == atout }.max { card -> card.points(atout) }
    hand.findAll { card ->
      (card.suite == atout) && (card.points(atout) > highestAtout.points(atout))
    }
  }

  private boolean haveAtout(Suite atout)
  {
    hand.find { card -> card.suite == atout }
  }
}
