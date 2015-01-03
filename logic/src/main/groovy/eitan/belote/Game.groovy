package eitan.belote

import groovy.util.logging.Slf4j

import static eitan.belote.ScoreUtils.*

@Slf4j
class Game implements Emitter
{
  Partie partie
  Dealer dealer = new Dealer()

  Team getTeam1() { partie?.team1 }
  Team getTeam2() { partie?.team2 }

  Map<Team, Integer> scores = [:]

  Suit atout
  Player committedPlayer

  Player starter, currentPlayer

  List<Round> rounds = []
  Round currentRound = null

  boolean done  // do i need this field?
  Team teamWithBeloteRebelote = null


  def init()
  {
    dealer.setActorRef(this.actorRef)
    starter = partie.starter
    teamWithBeloteRebelote = null
    done = false
    initScores()
  }

  def begin()
  {
    init()
    dealCards()
    dealer.turnUpCandidateCard()
    startSelectionPhase1()
  }

  def startSelectionPhase1()
  {
    currentPlayer = null
    continueSelectionPhase1()
  }
  def continueSelectionPhase1()
  {
    Player player = nextPlayer()
    if (player == null) {
      startSelectionPhase2()
    }
    else
    {
      player.offer(this, dealer.candidate)
    }
  }

  def pass()
  {
    emit('playerDecision', [currentPlayer, false, dealer.candidate.suit])
    continueSelectionPhase1()
  }
  def passDeuxFois()
  {
    emit('playerDecision', [currentPlayer, false, null])
    continueSelectionPhase2()
  }

  def envoi()
  {
    envoi(dealer.candidate.suit)
  }

  def envoi(Suit suit)
  {
    this.committedPlayer = currentPlayer
    this.atout = suit

    emit('playerDecision', [committedPlayer, true, suit])

    dealer.dealRemaining(players(), committedPlayer)

    Player playerWithBeloteRebelote = beloteRebelote()
    if (playerWithBeloteRebelote) {
      teamWithBeloteRebelote = playerWithBeloteRebelote.team
    }

    startPlayPhase()
  }

  def startSelectionPhase2()
  {
    currentPlayer = null
    continueSelectionPhase2()
  }
  def continueSelectionPhase2()
  {
    Player player = nextPlayer()
    if (player == null)
    {
      players().each { p ->
        p.gameDone()
      }

      done = true
      partie.gameDone(this)
    }
    else
    {
      player.offer(this)
    }
  }

  private void initScores() {
    scores[team1] = 0
    scores[team2] = 0
  }

  private void dealCards()
  {
    log.info("Dealing cards..")
    dealer.deal(players())
  }

  private Player nextPlayer()
  {
    if (currentPlayer == null)
    {
      currentPlayer = starter
      return starter
    }

    def nextOne = partie.nextPlayer(currentPlayer)
    currentPlayer = (nextOne == starter ? null : nextOne)
    currentPlayer
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
  def getLosingTeam()
  {
    assert done

    (winningTeam == team1) ? team2 :
        (winningTeam == team2) ? team1 :
            null
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
      log.info("${losingTeam} are capot")
      addCapotCredit()
    } else {
      addDixDedere(lastRound.winner.team)
    }

    addBeloteRebelote()

    if (dedans()) {
      log.info("${committedTeam} are dedans")
      scores[otherTeam] += scores[committedTeam]
      scores[committedTeam] = 0
    }
  }

  private void addDixDedere(Team team)
  {
    scores[team] += 10
  }

  // should be private but can't mock private methods (spock limitation)
  void addBeloteRebelote()
  {
    if (teamWithBeloteRebelote) {
      scores[teamWithBeloteRebelote] += 20
    }
  }

  private void addCapotCredit()
  {
    assert capot()
    Team creditee = ( scores[team2] == 0 ) ? team1 : team2
    scores[creditee] += 100
  }

  boolean capot() {
    def rawScores = rounds.inject([(team1): 0, (team2): 0], { acc, round ->
      acc[round.winner.team] += round.points
      acc
    })
    rawScores[team1] == 0 || rawScores[team2] == 0
  }

  boolean dedans() {
    boolean isDedans = scores[committedTeam] < scores[otherTeam]
    if (isDedans) {
      log.info("dedans with score: ${scores[committedTeam]}/${scores[otherTeam]}")
    }
    isDedans
  }

  boolean litige() {
    scores[committedTeam] == scores[otherTeam]
  }

  boolean isLastRound() {
    rounds.size() == 8
  }

  boolean forfeited() {
    done && !committedPlayer
  }

  def startPlayPhase()
  {
    starter = nextStarter()
    currentRound = new Round(game: this, actorRef: this.actorRef)
    currentPlayer = null
    continuePlayPhase()
  }

  def continuePlayPhase()
  {
    Player player = nextPlayer()
    if (player == null)
    {
      if (isLastRound())
      {
        finalizeScore()
        partie.gameDone(this)
      }
      else
      {
        startPlayPhase()
      }
    }
    else
    {
      player.play(this, currentRound)
    }
  }

  def playerChooses(Card card)
  {
    boolean lastRound = rounds.size() == 7
    boolean lastCard = currentRound.size() == 3
    def delay = lastRound && lastCard ? Delay.ExtraLong :
        lastCard == 3 ? Delay.Long : Delay.Standard
    currentPlayer.playCard(card, delay)
    currentRound = currentRound.nextPlay(card, currentPlayer)
    continuePlayPhase()
  }


  Player nextStarter()
  {
    if (rounds.empty) {
      starter
    } else {
      rounds.last().winner
    }
  }

  void roundDone(Round round)
  {
    rounds << round
    updateScore(round)
  }

  private void updateScore(Round round)
  {
    scores[round.winner.team] += round.points
    emit('gameUpdate', [team1, scores[team1], team2, scores[team2]])
  }

  int scoreAdjustment(Team team)
  {
    def score = scores[team]
    def roundedScore = roundScore(score)
    roundedScore - score
  }

  Player beloteRebelote()
  {
    assert atout != null
    players().find { player -> player.hasBeloteRebelote(atout) }
  }

  List<Player> players()
  {
    def players = []
    currentPlayer = null
    withEachPlayer { player ->
      players << player
    }
    players
  }

  void withEachPlayer(Closure closure) {
    currentPlayer = null
    Player player = nextPlayer()
    while (player != null)
    {
      closure.call(player)
      player = nextPlayer()
    }
  }
  def withEachPlayerUntilReturns(Closure closure) {
    currentPlayer = null
    Player player = nextPlayer()
    def response = null
    while (player != null && !response)
    {
      response = closure.call(player)
      player = nextPlayer()
    }
    response
  }

}
