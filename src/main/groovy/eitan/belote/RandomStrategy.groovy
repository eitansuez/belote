package eitan.belote

class RandomStrategy implements Strategy
{
  Player player

  @Override
  boolean envoi(Card candidate)
  {
    false  // pass all the time
  }

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    validCards.first()
  }
}
