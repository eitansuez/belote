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
    selectNextStarter()
    games << game
    game
  }

  private void selectNextStarter()
  {
    if (starter == null) {
      starter = team1.first
      return
    }
//
//    def previousStarter = starter;
//    def team = (previousStarter.team == team1) ? team2 : team1
//    starter = (previousStarter.team.first == previousStarter) ? team.first : team.second
  }
}
