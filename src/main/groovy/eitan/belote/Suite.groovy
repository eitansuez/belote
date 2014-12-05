package eitan.belote

enum Suite
{
  Coeur, Carreau, Pique, Trefle

  List cards = []

  Suite() {
    CardType.values().each { cardType ->
      cards << new Card(type: cardType, suite: this)
    }
  }

}
