package eitan.belotte

class Deck
{
  List cards = []

  Deck() {
    Suite.values().each { suite ->
      suite.cards.each { card ->
        cards << card
      }
    }
  }

  Card takeCard()
  {
    cards.remove(randomIndex())
  }

  private int randomIndex() {
    Math.random() * cards.size()
  }

}
