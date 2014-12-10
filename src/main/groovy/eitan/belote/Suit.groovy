package eitan.belote

enum Suit
{
  Coeur, Carreau, Pique, Trefle

  List<Card> cards = []

  Suit() {
    CardType.values().each { cardType ->
      cards << new Card(type: cardType, suit: this)
    }
  }

  @Override
  String toString() {
    name()
  }
}
