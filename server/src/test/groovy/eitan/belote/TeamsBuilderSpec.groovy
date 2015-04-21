package eitan.belote

import spock.lang.Specification

class TeamsBuilderSpec extends Specification
{
  def "initial teams should be all bots"() {
    expect:
    new TeamsBuilder().teams() == [team1: [first: 'Bot1', second: 'Bot2'],
                                   team2: [first: 'Bot3', second: 'Bot4']]
  }

  def "can get list of player names"() {
    expect:
    new TeamsBuilder().playerNames() == ['Bot1', 'Bot2', 'Bot3', 'Bot4']
  }

  def "should get player at team and position"(teamName, positionName, expectedPlayerName) {
    expect:
    new TeamsBuilder().playerAt(teamName, positionName) == expectedPlayerName

    where:
    teamName | positionName | expectedPlayerName
    'team1'  | 'first'      | 'Bot1'
    'team1'  | 'second'     | 'Bot2'
    'team2'  | 'first'      | 'Bot3'
    'team2'  | 'second'     | 'Bot4'
  }

  def "should set player at team and position"() {
    given:
    def builder = new TeamsBuilder()

    when:
    builder.setPlayerAt('team1', 'first', 'John')

    then:
    builder.playerAt('team1', 'first') == 'John'
  }

  def "should give position of player name as hash"(player, teamName, positionName) {
    expect:
    new TeamsBuilder().positionOf(player) == [team: teamName, position: positionName]

    where:
    player | teamName | positionName
    'Bot1' | 'team1'  | 'first'
    'Bot2' | 'team1'  | 'second'
    'Bot3' | 'team2'  | 'first'
    'Bot4' | 'team2'  | 'second'
  }

  def "should give null if no player found"() {
    expect:
    new TeamsBuilder().positionOf("John Doe") == null
  }
}
