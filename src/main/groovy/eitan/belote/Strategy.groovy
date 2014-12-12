package eitan.belote

interface Strategy
{
  boolean envoi(Card candidate)
  Card chooseCard(Set<Card> validCards, Round round)
}
