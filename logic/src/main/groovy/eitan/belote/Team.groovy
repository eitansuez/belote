package eitan.belote

class Team
{
  Player first
  Player second

  void setFirst(Player p)
  {
    this.first = p
    p.team = this
  }

  void setSecond(Player p)
  {
    this.second = p
    p.team = this
  }

  @Override
  String toString() {
    "${first}-${second}"
  }
}
