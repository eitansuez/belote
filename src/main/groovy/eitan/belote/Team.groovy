package eitan.belote

class Team
{
  Player first
  Player second

  int score = 0

  public void setFirst(Player p)
  {
    this.first = p
    p.team = this
  }

  public void setSecond(Player p)
  {
    this.second = p
    p.team = this
  }
}
