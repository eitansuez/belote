package eitan.belote

enum CardType
{
  Sept(),
  Huit(),
  Neuf(0,14),
  Dix(10),
  Valet(2,20),
  Dame(3),
  Roi(4),
  Ace(11)

  int points, pointsWhenAtout

  CardType() {
    this(0)
  }

  CardType(int value) {
    this(value, value)
  }

  CardType(int value, int valueAsAtout) {
    this.points = value
    this.pointsWhenAtout = valueAsAtout
  }

  @Override
  String toString() {
    name()
  }
}
