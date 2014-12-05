package eitan.belote

class Game
{
  Deck deck = new Deck()

  List<Player> team1 = []
  List<Player> team2 = []

  def start()
  {
    deck.deal(team1[0], team1[1], team2[0], team2[1])
  }

}
