package eitan.belote

class Game
{
  Deck deck = new Deck()
  Team team1, team2
  Suite atout
  def committedPlayer

  def start()
  {
    dealCards()
  }

  private void dealCards()
  {
    deck.deal(team1.first, team2.first, team1.second, team2.second)
  }

  def envoi(Suite suite, Player p)
  {
    this.committedPlayer = p
    this.atout = suite
  }

  def getCommittedTeam()
  {
    committedPlayer.team
  }

  int points(Card card)
  {
    card.points(atout)
  }
}
