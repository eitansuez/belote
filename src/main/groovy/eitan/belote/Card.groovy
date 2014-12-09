package eitan.belote

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Card
{
  Suite suite
  CardType type

  def points()
  {
    return type.points
  }

  def points(Suite atout)
  {
    atout == suite ? type.pointsWhenAtout : type.points
  }

  @Override
  String toString() {
    "${type} de ${suite}"
  }
}
