package eitan.belote

class Card
{
  Suite suite
  CardType type

  def title()
  {
    "${type.name()} de ${suite.name()}"
  }

}
