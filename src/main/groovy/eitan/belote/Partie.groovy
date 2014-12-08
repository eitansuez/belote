package eitan.belote

class Partie
{
  Team team1, team2
  Map<Team, Integer> scores = [:]
  Player starter
  List<Game> games = []

  def begin()
  {
    initScores()
  }

  private void initScores() {
    scores[team1] = 0
    scores[team2] = 0
  }

  boolean done()
  {
    scores[team1] >= 1000 || scores[team2] >= 1000
  }

  def nextGame()
  {
    def game = new Game(partie: this)
    starter = nextStarter()
    games << game
    game
  }

  def gameDone()
  {
    def game = games.last()
    assert game.done

    transferScores(game)
  }

  private void transferScores(game)
  {
    scores[team1] += round(game.scores[team1])
    scores[team2] += round(game.scores[team2])
  }

  int round(int score) {
    Math.round(score/10) * 10
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
    def players = [team1.first, team2.first, team1.second, team2.second]
    def index = players.indexOf(from)
    index += 1
    index %= 4
    players[index]
  }
}
