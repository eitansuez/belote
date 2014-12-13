package eitan.belote

import static eitan.belote.Suit.Trefle

class TestStrategy implements Strategy
{
  Player player

  @Override
  boolean envoi(Card candidate)
  {
    false
  }

  @Override
  Suit envoi()
  {
    null
  }

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    round.highest(validCards)
  }
}
