package eitan.belote

import groovy.util.logging.Slf4j

import static eitan.belote.ScoreUtils.roundScore

@Slf4j
class Partie implements Emitter
{
  Team team1, team2
  def players = []
  Map<Team, Integer> scores = [:]
  Player starter
  List<Game> games = []

  Game currentGame = null

  def init()
  {
    players = [team1.first, team2.first, team1.second, team2.second]
    initScores()
  }

  def begin()
  {
    init()

    emit('partieStarts', [team1, team2, players])
    startNextGame()
  }

  private void initScores() {
    scores[team1] = 0
    scores[team2] = 0
  }

  boolean done()
  {
    scores[team1] >= 1000 || scores[team2] >= 1000
  }

  Team getWinner()
  {
    assert done()
    if (scores[team1] > scores[team2]) {
      return team1
    }
    if (scores[team2] > scores[team1]) {
      return team2
    }

    def lastGame = games.last()
    def scoreAdjustment1 = lastGame.scoreAdjustment(team1)
    def scoreAdjustment2 = lastGame.scoreAdjustment(team2)
    if (scoreAdjustment1 < scoreAdjustment2) {
      return team1
    } else if (scoreAdjustment2 < scoreAdjustment1) {
      return team2
    }
    return null // signifies a tie
  }

  // method setup like this to allow tests to inject a spy
  Game nextGame(game = new Game(partie: this, actorRef: this.actorRef))
  {
    starter = nextStarter()
    games << game
    currentGame = game
    game
  }

  Game startNextGame()
  {
    nextGame()
    log.info("Game #${games.size()} about to begin..")
    currentGame.begin()
  }

  def gameDone(Game game)
  {
    emit('gameEnds', [game.winningTeam])

    if (!game.forfeited()) {
      transferScores(game)
    }

    if (done())
    {
      emit('partieEnds', winner)
    }
    else
    {
      startNextGame()
    }
  }

  private void transferScores(Game game)
  {
    assert game.done
    if (game.litige()) {
      def otherTeam = game.otherTeam
      scores[otherTeam] += roundScore(game.scores[otherTeam])
      log.info("${game.committedTeam} is litige")
    }
    else
    {
      int thisGameScoreTeam1 = roundScore(game.scores[team1])
      scores[team1] += thisGameScoreTeam1
      int thisGameScoreTeam2 = roundScore(game.scores[team2])
      scores[team2] += thisGameScoreTeam2

      def prevGame = previousGame(game)
      while (prevGame?.litige())
      {
        if (game.winningTeam == prevGame.committedTeam) {
          scores[game.committedTeam] += roundScore(prevGame.scores[game.committedTeam])
          log.info("${game.committedTeam} recuperates its points from last game")
        } else {
          scores[game.otherTeam] += roundScore(prevGame.scores[game.committedTeam])
          log.info("${game.otherTeam} gains points from other team that were held litige in previous game")
        }
        prevGame = previousGame(prevGame)
      }
    }

    emit('partieUpdate', [team1, scores[team1], team2, scores[team2]])
  }

  Game previousGame(Game game)
  {
    int index = games.indexOf(game) - 1
    while (index >= 0)
    {
      if (!games[index].forfeited()) {
        return games[index]
      }
      index -= 1
    }
    null
  }

  private Player nextStarter()
  {
    if (starter == null) {
      return team1.first
    }

    nextPlayer(starter)
  }

  Player nextPlayer(Player from)
  {
    def index = players.indexOf(from)
    index = (index + 1) % 4
    players[index]
  }
}
