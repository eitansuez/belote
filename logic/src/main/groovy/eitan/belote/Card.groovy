package eitan.belote

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Card
{
  Suit suit
  CardType type

  def points()
  {
    return type.points
  }

  def points(Suit atout)
  {
    atout == suit ? type.pointsWhenAtout : type.points
  }

  static Card fromName(String name)
  {
    def (typeName, discard, suitName) = name.split("_")
    new Card(type: CardType.valueOf(typeName), suit: Suit.valueOf(suitName))
  }

  @Override
  String toString() {
    "${type} de ${suit}"
  }
}
