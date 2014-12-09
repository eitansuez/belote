package eitan.belote

class Team
{
  Player first
  Player second

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

  @Override
  String toString() {
    "${first}-${second}"
  }
}
