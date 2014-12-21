package eitan.belote

import spock.lang.Specification

class TeamSpec extends Specification
{
  Team team

  def setup()
  {
    Player p1 = new Player(name: "john")
    Player p2 = new Player(name: "jane")
    team = new Team(first: p1, second: p2)
  }

  def "team players assigned"()
  {
    expect:
    team.first.name == 'john'
    team.second.name == 'jane'
  }

  def "team toString cites members"()
  {
    expect:
    team.toString() == "john-jane"
  }
}
