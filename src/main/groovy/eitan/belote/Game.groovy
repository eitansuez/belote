package eitan.belote

class Game
{
  Deck deck = new Deck()
  Team team1, team2

  def start()
  {
    dealCards()
  }

  private void dealCards()
  {
    deck.deal(team1.first, team2.first, team1.second, team2.second)
  }

}
