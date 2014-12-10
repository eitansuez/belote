package eitan.belote

class TestStrategy implements Strategy
{
  Player player

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    round.highest(validCards)
  }
}
