package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suite.Trefle

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

  def "partie rotate starter with each new game"()
  {
    when:
    partie.begin()
    def game = partie.nextGame()
    game.begin()

    then:
    game.starter == eitan

    when:
    game = partie.nextGame()
    game.begin()

    then:
    game.starter == johnny

    when:
    game = partie.nextGame()
    game.begin()

    then:
    game.starter == rony

    when:
    game = partie.nextGame()
    game.begin()

    then:
    game.starter == corinne

    when:
    game = partie.nextGame()
    game.begin()

    then:
    game.starter == eitan
  }

  def "game rounded score transfers to partie"()
  {
    given:
    partie.begin()
    playGameWithScoreBeforeFinalize(75, 77)

    when:
    partie.gameDone()

    then:
    partie.scores[partie.team1] == 90
    partie.scores[partie.team2] == 80
  }

  private playGameWithScoreBeforeFinalize(int score1, int score2)
  {
    def game = partie.nextGame()
    game.begin()
    game.envoi(Trefle, eitan)
    game.playRandomly()
    game.scores[game.team1] = score1
    game.scores[game.team2] = score2
    game.hands.last().winner = eitan
    game.finalizeScore()
    game
  }

  def "game score accumulates across multiple games"()
  {
    given:
    partie.begin()
    playGameWithScoreBeforeFinalize(75, 77)
    partie.gameDone()
    playGameWithScoreBeforeFinalize(100, 52)

    when:
    partie.gameDone()

    then:
    partie.scores[partie.team1] == 200
    partie.scores[partie.team2] == 130
  }

}
