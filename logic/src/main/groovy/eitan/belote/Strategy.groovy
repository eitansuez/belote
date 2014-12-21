package eitan.belote

interface Strategy
{
  boolean envoi(Card candidate)
  Suit envoi()  // for second round
  Card chooseCard(Set<Card> validCards, Round round)
}
