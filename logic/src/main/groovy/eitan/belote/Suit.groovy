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

  static Suit fromName(String name)
  {
    valueOf(name.toLowerCase().capitalize())
  }


  @Override
  String toString() {
    name()
  }
}
