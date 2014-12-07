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
    starter = team1.first
  }

  private void initScores() {
    scores[team1] = 0
    scores[team2] = 0
  }

  boolean done()
  {
    scores[team1] >= 1000 || scores[team2] >= 1000
  }

  def startGame()
  {
    def game = new Game(partie: this)
    game.begin()
    games << game
  }
}
