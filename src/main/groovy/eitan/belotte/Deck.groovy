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

  List takeCards(int howMany)
  {
    List dealt = []
    howMany.times { dealt << takeCard() }
    dealt
  }

  private int randomIndex()
  {
    Math.random() * cards.size()
  }

  def deal(players)
  {
    players.each { player ->
      player.dealCards(takeCards(3))
    }

    players.each { player ->
      player.dealCards(takeCards(2))
    }
  }

  def dealRemaining(players)
  {
    assert size() == 12

    players.each { player ->
      player.dealCards(takeCards(3))
    }
  }

  def size() {
    cards.size()
  }
}
