package eitan.belote

import spock.lang.Specification

class PartieSpec extends Specification
{
  def Partie partie
  def Player eitan, johnny, corinne, rony

  def setup()
  {
    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")

    partie = new Partie(
        team1: new Team(first: eitan, second: rony),
        team2: new Team(first: johnny, second: corinne))

  }

  def "partie should have teams"()
  {
    expect:
    partie.team1.first == eitan
    partie.team1.second == rony
  }

  def "partie initial scores reset"()
  {
    when:
    partie.begin()

    then:
    partie.scores[partie.team1] == 0
    partie.scores[partie.team2] == 0
  }

  def "partie should be done if one of the teams has arrived at or crossed 1000"()
  {
    given:
    partie.begin()

    when:
    partie.scores[partie.team1] = 1010

    then:
    partie.done()
  }

  def "partie not done if neither team has reached 1000"()
  {
    given:
    partie.begin()

    when:
    partie.scores[partie.team1] = 550
    partie.scores[partie.team2] = 330

    then:
    !partie.done()
  }

  def "partie play first game"()
  {
    given:
    partie.begin()
    def game = partie.nextGame()

    when:
    game.begin()

    then:
    game.team1 == partie.team1
    game.starter == eitan
  }



}
