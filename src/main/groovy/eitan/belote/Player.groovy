package eitan.belote

class Player
{
  String name
  List<Card> cards = new ArrayList<Card>()
  Team team

  def dealCard(Card card)
  {
    cards << card
  }

  def dealCards(List<Card> cards)
  {
    cards.each { card ->
      dealCard(card)
    }
  }

  def playRandomCard()
  {
    playCard(randomIndex())
  }
  private int randomIndex()
  {
    Math.random() * cards.size()
  }


  def hand()
  {
    cards
  }

  @Override
  String toString() {
    "Player: $name"
  }

  def playCard(int index)
  {
    cards.remove(index)
  }
}
