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

  static Suit interpretSuitFromAcronym(String acronym)
  {
    def map = ['s' : Pique, 'c' : Trefle, 'h' : Coeur, 'd' : Carreau]
    assert acronym != null && map.keySet().contains(acronym)
    map[acronym]
  }

  @Override
  String toString() {
    name()
  }
}
