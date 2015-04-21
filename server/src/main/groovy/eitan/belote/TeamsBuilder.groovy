package eitan.belote

class TeamsBuilder
{
  def teams = [team1: [first: 'Bot1', second: 'Bot2'],
               team2: [first: 'Bot3', second: 'Bot4']]

  def teams() {
    teams
  }

  def playerNames() {
    [teams.team1.first, teams.team1.second, teams.team2.first, teams.team2.second]
  }

  def playerAt(teamName, positionName) {
    teams[teamName][positionName]
  }

  def setPlayerAt(teamName, positionName, playerName) {
    teams[teamName][positionName] = playerName
  }

  def positionOf(targetPlayer) {
    def teamName = teams.find { teamName, positions ->
      positions.find { positionName, player ->
        player == targetPlayer
      }
    }?.key
    if (!teamName) return null

    def positionName = teams[teamName].find { positionName, player ->
      player == targetPlayer
    }.key

    [team: teamName, position: positionName]
  }
}
