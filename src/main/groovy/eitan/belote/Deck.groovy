package eitan.belote

class Deck
{
  List<Card> cards = []

  Deck() {
    Suit.values().each { suit ->
      suit.cards.each { card ->
        cards << card
      }
    }
  }

  Card takeCard()
  {
    cards.remove(randomIndex())
  }
  private int randomIndex()
  {
    Math.random() * cards.size()
  }


  List takeCards(int howMany)
  {
    List dealt = []
    howMany.times { dealt << takeCard() }
    dealt
  }

  def deal(List<Player> players)
  {
    assert players?.size() == 4

    players.each { player ->
      player.dealCards(takeCards(3))
    }

    players.each { player ->
      player.dealCards(takeCards(2))
    }
  }

  def dealRemaining(List<Player> players)
  {
    assert size() == 12

    players.each { player ->
      player.dealCards(takeCards(3))
    }
  }

  def size() {
    cards?.size()
  }

  def full()
  {
    size() == 32
  }
  def empty()
  {
    size() == 0
  }
}
