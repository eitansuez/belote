package eitan.belotte

class Player
{
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
}
