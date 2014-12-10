package eitan.belote

interface Strategy
{
  Card chooseCard(Set<Card> validCards, Round round)
}
