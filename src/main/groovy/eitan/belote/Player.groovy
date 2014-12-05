package eitan.belote

class Player
{
  String name
  List<Card> cards = new ArrayList<Card>()

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

  def hand()
  {
    cards
  }
}
