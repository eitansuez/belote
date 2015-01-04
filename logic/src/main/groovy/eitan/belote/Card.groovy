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

  boolean higherThan(Card otherCard, Suit atout)
  {
    assert otherCard.suit == suit  // assumes comparison of cards within the same suit
    int result = points(atout) <=> otherCard.points(atout)
    if (result != 0)
    {
      return result > 0 ? true : false
    }
    return type.ordinal() > otherCard.type.ordinal()
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
