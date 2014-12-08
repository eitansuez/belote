package eitan.belote

class Player
{
  String name
  List<Card> cards = new ArrayList<Card>()
  Team team

  def dealCard(Card card)
  {
    cards << card
  }

  def dealCards(List<Card> cards)
  {
    cards.each { card ->
      dealCard(card)
    }
  }

  def hand()
  {
    cards
  }

  @Override
  String toString() {
    "Player: $name"
  }

  def playCard(int index)
  {
    cards.remove(index)
  }

  def playCard(Card card)
  {
    if (cards.remove(card)) {
      return card
    }
    throw new NoSuchElementException("Player doesn't have card ${card} to play!")
  }

  Card validCard(List<Card> placed, Suite atout)
  {
    Card anyCard = cards.first()
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

      Card anyAtout = cards.find { card ->
        card.suite == atout
      }
      if (anyAtout != null) {
        return anyAtout
      }
      return anyCard
    }

    Card matchingSuite = cards.find { card -> card.suite == requested }
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

      Card anyAtout = cards.find { card ->
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
    Card higherAtout = cards.find { card ->
      (card.suite == atout) && (card.points(atout) > highestAtout.points(atout))
    }
    higherAtout
  }

  private boolean haveAtout(Suite atout)
  {
    cards.find { card -> card.suite == atout } != null
  }
}
