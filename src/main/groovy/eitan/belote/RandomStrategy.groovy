package eitan.belote

class RandomStrategy implements Strategy
{
  Player player

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    return validCards.first()
  }
}
