package eitan.belote

class TestStrategy implements Strategy
{
  Player player

  @Override
  boolean envoi(Card candidate)
  {
    return false
  }

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    round.highest(validCards)
  }
}
