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

  @Override
  String toString() {
    "${type} de ${suit}"
  }
}
