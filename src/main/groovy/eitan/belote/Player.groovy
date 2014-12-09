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

  Card validCard(List<Card> placed, Suite atout)
  {
    Card anyCard = hand.first()
    if (placed.empty)
    {
      return anyCard
    }

    Suite requested = placed.first().suite

    if (requested == atout)
    {
      Card higherAtout = findHigherAtout(placed, atout)
      if (higherAtout != null) {
        return higherAtout
      }

      Card anyAtout = hand.find { card ->
        card.suite == atout
      }
      if (anyAtout != null) {
        return anyAtout
      }
      return anyCard
    }

    Card matchingSuite = hand.find { card -> card.suite == requested }
    if (matchingSuite != null) {
      return matchingSuite
    }

    if (haveAtout(atout))
    {
      if (placed.find { card -> card.suite == atout }) {
        Card higherAtout = findHigherAtout(placed, atout)
        if (higherAtout != null) {
          return higherAtout
        }
      }

      Card anyAtout = hand.find { card ->
        card.suite == atout
      }
      if (anyAtout != null) {
        return anyAtout
      }

    }

    return anyCard

  }

  private Card findHigherAtout(List<Card> placed, Suite atout)
  {
    Card highestAtout = placed.findAll { card -> card.suite == atout }.max { card -> card.points(atout) }
    Card higherAtout = hand.find { card ->
      (card.suite == atout) && (card.points(atout) > highestAtout.points(atout))
    }
    higherAtout
  }

  private boolean haveAtout(Suite atout)
  {
    hand.find { card -> card.suite == atout } != null
  }
}
