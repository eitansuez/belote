package eitan.belote

class Card
{
  Suite suite
  CardType type

  def title()
  {
    "${type.name()} de ${suite.name()}"
  }

  def points()
  {
    return type.points
  }

  def points(Suite atout)
  {
    atout == suite ? type.pointsWhenAtout : type.points
  }

}
