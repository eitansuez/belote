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

    partie = GroovySpy(Partie, constructorArgs: [[team1: new Team(first: eitan, second: rony),
                                                  team2: new Team(first: johnny, second: corinne)]]) {
      startNextGame() >> {}
    }

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
    partie.init()

    then:
    partie.scores[partie.team1] == 0
    partie.scores[partie.team2] == 0
  }

  def "partie should be done if one of the teams has arrived at or crossed 1000"()
  {
    given:
    partie.init()

    when:
    partie.scores[partie.team1] = 1010

    then:
    partie.done()
  }

  def "partie should be done if other team has arrived at or crossed 1000"()
  {
    given:
    partie.init()

    when:
    partie.scores[partie.team2] = 1000

    then:
    partie.done()
  }

  def "partie not done if neither team has reached 1000"()
  {
    given:
    partie.init()

    when:
    partie.scores[partie.team1] = 550
    partie.scores[partie.team2] = 330

    then:
    !partie.done()
  }

  def "partie play first game"()
  {
    given:
    partie.init()
    def game = partie.nextGame()

    when:
    game.init()

    then:
    game.team1 == partie.team1
    game.starter == eitan
  }

  def "partie rotate starter with each new game"()
  {
    when:
    partie.init()
    def game = partie.nextGame()
    game.init()

    then:
    game.starter == eitan

    when:
    game = partie.nextGame()
    game.init()

    then:
    game.starter == johnny

    when:
    game = partie.nextGame()
    game.init()

    then:
    game.starter == rony

    when:
    game = partie.nextGame()
    game.init()

    then:
    game.starter == corinne

    when:
    game = partie.nextGame()
    game.init()

    then:
    game.starter == eitan
  }

  def "game rounded score transfers to partie"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 85
    game.scores[partie.team2] = 77

    when:
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 90
    partie.scores[partie.team2] == 80
  }

  def "game score accumulates across multiple games"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 85
    game.scores[partie.team2] = 77

    partie.gameDone(game)

    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 110
    game.scores[partie.team2] = 52

    when:
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 200
    partie.scores[partie.team2] == 130
  }



  def "can tell winner"()
  {
    given:
    partie.init()
    partie.scores[partie.team1] = 900
    partie.scores[partie.team2] = 300

    when:
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 130
    game.scores[partie.team2] = 32

    partie.gameDone(game)

    then:
    partie.done()
    partie.scores[partie.team1] == 1030
    partie.scores[partie.team2] == 330

    and:
    partie.winner == partie.team1
  }

  def "can tell winner when both teams cross 1000"()
  {
    given:
    partie.init()
    partie.scores[partie.team1] = 900
    partie.scores[partie.team2] = 980

    when:
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 110
    game.scores[partie.team2] = 52

    partie.gameDone(game)

    then:
    partie.done()
    partie.scores[partie.team1] == 1010
    partie.scores[partie.team2] == 1030

    and:
    partie.winner == partie.team2
  }

  def "can tell winner even when both teams cross 1000 with same rounded score"()
  {
    given:
    partie.init()
    partie.scores[partie.team1] = 900
    partie.scores[partie.team2] = 980

    when:
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 120
    game.scores[partie.team2] = 42
    partie.gameDone(game)

    then:
    partie.done()
    partie.scores[partie.team1] == 1020
    partie.scores[partie.team2] == 1020

    and:
    partie.winner == partie.team2
  }

  def "the rare partie tie"()
  {
    given:
    partie.init()
    partie.scores[partie.team1] = 900
    partie.scores[partie.team2] = 980

    when:
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 121
    game.scores[partie.team2] = 41
    partie.gameDone(game)

    then:
    partie.done()
    partie.scores[partie.team1] == 1020
    partie.scores[partie.team2] == 1020

    and:
    partie.winner == null
  }

  def "points withheld from envoyeur when litige"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81

    when:
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 0
    partie.scores[partie.team2] == 80
  }

  def "points returned to envoyeur after litige if win next game"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    when:
    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 140
    game.scores[partie.team2] = 22
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 220
    partie.scores[partie.team2] == 100
  }

  def "points given to opposing team when litigee loses next game"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    when:
    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 0
    game.scores[partie.team2] = 162
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 0
    partie.scores[partie.team2] == 320
  }

  def "multiple litiges in a row (de suite) for the same team accumulates points in abeyance, team eventually wins"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)
    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    when:
    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 140
    game.scores[partie.team2] = 22
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 300
    partie.scores[partie.team2] == 180
  }

  def "multiple mixed/alternating litiges in a row"()
  {
    given:
    partie.init()
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team2.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    when:
    game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 140
    game.scores[partie.team2] = 22
    partie.gameDone(game)

    then:
    partie.scores[partie.team1] == 300
    partie.scores[partie.team2] == 180
  }

  def "previous game should be previous played"()
  {
    given:
    partie.init()

    when:
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team1.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    def secondGame = partie.nextGame()
    secondGame.done = true
    secondGame.committedPlayer = partie.team2.first
    secondGame.scores[partie.team1] = 81
    secondGame.scores[partie.team2] = 81
    partie.gameDone(secondGame)

    then:
    partie.previousGame(secondGame) == game
    partie.previousGame(game) == null
  }

  def "previous game excludes games that were forfeited (must have been played)"()
  {
    given:
    partie.init()

    when:
    def firstGame = partie.nextGame()
    firstGame.committedPlayer = null
    firstGame.done = true

    then:
    firstGame.forfeited()

    when:
    def game = partie.nextGame()
    game.done = true
    game.committedPlayer = partie.team2.first
    game.scores[partie.team1] = 81
    game.scores[partie.team2] = 81
    partie.gameDone(game)

    then:
    partie.previousGame(game) == null
  }

}
