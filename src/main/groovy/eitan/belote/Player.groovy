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
        card.suit == round.atout
      }
      if (allAtouts) {
        return allAtouts
      }
      return hand
    }

    Set<Card> matchingSuit = hand.findAll { card -> card.suit == round.requestedSuit() }
    if (matchingSuit) {
      return matchingSuit
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
        card.suit == round.atout
      }
      if (allAtouts) {
        return allAtouts
      }

    }

    hand
  }

  private Set<Card> findHigherAtouts(Round round)
  {
    Suit atout = round.atout
    Card highestAtout = round.highest(round.atouts())
    hand.findAll { card ->
      (card.suit == atout) && (card.points(atout) > highestAtout.points(atout))
    }
  }

  private boolean haveAtout(Suit atout)
  {
    hand.find { card -> card.suit == atout }
  }
}
