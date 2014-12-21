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

  static def map = ['s' : Pique, 'c' : Trefle, 'h' : Coeur, 'd' : Carreau]

  static boolean isValidAcronym(String acronym)
  {
    acronym != null && map.keySet().contains(acronym)
  }
  static Suit interpretSuitFromAcronym(String acronym)
  {
    assert isValidAcronym(acronym)
    map[acronym]
  }

  @Override
  String toString() {
    name()
  }
}