package eitan.belote

enum Suit
{
  Coeur, Pique, Carreau, Trefle

  List<Card> cards = []

  Suit() {
    CardType.values().each { cardType ->
      cards << new Card(type: cardType, suit: this)
    }
  }

  static Suit fromName(String name)
  {
    valueOf(name.toLowerCase().capitalize())
  }

}
