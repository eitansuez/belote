package eitan.belote

import groovy.util.logging.Log

@Log
class Game
{
  Partie partie
  Deck deck = new Deck()
  Team team1, team2
  Suite atout
  def committedPlayer
  def scores = [:]
  List<Round> rounds = []
  Player starter
  boolean done  // do i need this field?

  def begin()
  {
    team1 = partie.team1
    team2 = partie.team2
    starter = partie.starter

    done = false
    initScores()
    dealCards()
  }

  private void initScores() {
    scores[team1] = 0
    scores[team2] = 0
  }

  private void dealCards()
  {
    log.info("Dealing cards..")
    deck.deal(players())
  }

  def envoi(Suite suite, Player player)
  {
    this.committedPlayer = player
    this.atout = suite
    log.info("Game starting with ${player} envoie a ${suite}")
    dealRemainingCards()
  }

  private void dealRemainingCards()
  {
    log.info("Dealing remaining cards..")
    deck.dealRemaining(players())
  }

  private Player[] players()
  {
    [team1.first, team2.first, team1.second, team2.second]
  }

  def getCommittedTeam()
  {
    committedPlayer.team
  }
  def getOtherTeam()
  {
    (committedTeam == team1) ? team2 : team1
  }
  def getWinningTeam()
  {
    assert done

    if (scores[team1] > scores[team2]) {
      return team1
    } else if (scores[team2] > scores[team1]) {
      return team2
    }
    null // a tie implies no winning team
  }

  int points(Card card)
  {
    card.points(atout)
  }

  void finalizeScore()
  {
    assert isLastRound()
    done = true

    Round lastRound = rounds.last()
    if (capot()) {
      addCapotCredit()
      return
    }
    addDixDedere(lastRound.winner.team)

    if (dedans())
    {
      scores[committedTeam] = 0
      scores[otherTeam] = 162
    }
  }

  private void addDixDedere(Team team)
  {
    scores[team] += 10
  }

  private void addCapotCredit()
  {
    assert capot()
    Team creditee = ( scores[team2] == 152 ) ? team2 : team1
    scores[creditee] += 100
  }

  boolean capot() {
    scores[team1] == 0 || scores[team2] == 0
  }

  boolean dedans() {
    scores[committedTeam] < 81
  }

  boolean litige() {
    scores[committedTeam] == 81
  }

  boolean isLastRound() {
    rounds.size() == 8
  }

  void playRandomly()
  {
    8.times { playRandomRound() }
  }

  void playRandomRound()
  {
    def cards = [], players = []

    Player player = starter
    4.times {
      Set<Card> validCards = player.validCards(cards, atout)
      cards << player.playCard(validCards.first())
      players << player
      player = partie.nextPlayer(player)
    }

    playRound(cards, players)
  }

  void playRound(List<Card> cards, List<Player> players)
  {
    assert cards.size() == 4
    assert players.size() == 4

    def round = new Round(cards: cards, players: players, atout: atout)
    round.resolve()

    rounds << round

    updateScore(round)

    if (!isLastRound())
    {
      starter = round.winner
    }
  }

  private void updateScore(Round round)
  {
    scores[round.winner.team] += round.points
  }


}
