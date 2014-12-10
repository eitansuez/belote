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
    name
  }

  def playCard(Card card)
  {
    assert hand.contains(card)

    if (hand.remove(card)) {
      return card
    }
  }

  Set<Card> validCards(Round round)
  {
    if (round.empty)
    {
      return hand
    }

    if (round.requestedAtout())
    {
      Set<Card> higherAtouts = findHigherAtouts(round)
      if (higherAtouts) {
        return higherAtouts
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suite == round.atout
      }
      if (allAtouts) {
        return allAtouts
      }
      return hand
    }

    Set<Card> matchingSuite = hand.findAll { card -> card.suite == round.requestedSuite() }
    if (matchingSuite) {
      return matchingSuite
    }

    if (haveAtout(round.atout))
    {
      if (round.containsAtout())
      {
        Set<Card> higherAtouts = findHigherAtouts(round)
        if (higherAtouts) {
          return higherAtouts
        }
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suite == round.atout
      }
      if (allAtouts) {
        return allAtouts
      }

    }

    hand
  }

  private Set<Card> findHigherAtouts(Round round)
  {
    Suite atout = round.atout
    Card highestAtout = round.highest(round.atouts())
    hand.findAll { card ->
      (card.suite == atout) && (card.points(atout) > highestAtout.points(atout))
    }
  }

  private boolean haveAtout(Suite atout)
  {
    hand.find { card -> card.suite == atout }
  }
}
