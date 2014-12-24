package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Game implements Emitter
{
  Partie partie
  Dealer dealer = new Dealer()

  Team team1, team2
  Map<Team, Integer> scores = [:]

  Suit atout
  Player committedPlayer

  Player starter

  List<Round> rounds = []

  boolean done  // do i need this field?

  def begin()
  {
    dealer.setActorRef(this.actorRef)

    team1 = partie.team1
    team2 = partie.team2
    starter = partie.starter

    done = false
    initScores()
    dealCards()
  }

  def selectionPhase1(Card candidate)
  {
    log.info("envoi a ${candidate.suit}?")

    def response = withEachPlayerUntilReturns { Player player ->
      if (player.envoi(candidate)) {
        envoi(candidate.suit, player)
        return [player, candidate]
      } else {
        return null
      }
    }

    response
  }

  def selectionPhase2()
  {
    log.info("second round of atout selection begins..")

    def response = withEachPlayerUntilReturns { Player player ->
      Suit suit = player.envoi()
      if (suit) {
        envoi(suit, player)
        return [player, suit]
      } else {
        return null
      }
    }

    if (!response) {

      emit("gameForfeit", [])

      players().each { player ->
        player.gameDone()
      }

      done = true
    }

    response
  }

  private void initScores() {
    scores[team1] = 0
    scores[team2] = 0
  }

  private void dealCards()
  {
    log.info("Dealing cards..")
    dealer.deal(players())
    showPlayerCards()
  }

  private void showPlayerCards()
  {
    withEachPlayer { Player player ->
      player.showHand()
    }
  }

  def envoi(Suit suit, Player player)
  {
    this.committedPlayer = player
    this.atout = suit
    log.info("Game starting with ${player} envoie a ${suit}")

    dealer.dealRemaining(players(), committedPlayer)
    showPlayerCards()
  }

  List<Player> players()
  {
    def players = []
    withEachPlayer { player ->
      players << player
    }
    players
  }

  void withEachPlayer(Closure closure) {
    Player player = starter
    4.times {
      closure.call(player)
      player = partie.nextPlayer(player)
    }
  }
  def withEachPlayerUntilReturns(Closure closure) {
    Player player = starter
    def response = null
    def count = 1
    while (!response && count <= 4) {
      response = closure.call(player)
      player = partie.nextPlayer(player)
      count++
    }
    response
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
    try
    {
      assert isLastRound()
      done = true

      Round lastRound = rounds.last()
      if (capot()) {
        log.info("${losingTeam} are capot")
        addCapotCredit()
        return
      }
      addDixDedere(lastRound.winner.team)

      if (dedans())
      {
        log.info("${committedTeam} are dedans")
        scores[committedTeam] = 0
        scores[otherTeam] = 162
      }
    }
    finally {
      showScore()
    }
  }

  private void showScore()
  {
    log.info("${team1}: ${scores[team1]} / ${team2}: ${scores[team2]}")
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

  void playRandomly()
  {
    8.times { playRandomRound() }
  }

  void playRandomRound()
  {
    log.info("Round #${rounds.size()+1}")

    def round = new Round(atout: atout, actorRef: this.actorRef)

    withEachPlayer { Player player ->
      Card selected = player.chooseCard(round)
      def card = player.playCard(selected)
      log.info("${player} plays ${card}")

      round = round.newRound(card, player)
    }

    // TODO:  round.resolve should notify game that it's done
    roundDone(round)
  }

  void roundDone(Round round)
  {
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
    emit("gameUpdate", [team1, scores[team1], team2, scores[team2]])
  }

  int scoreAdjustment(Team team)
  {
    def score = scores[team]
    def roundedScore = Partie.roundScore(score)
    roundedScore - score
  }

  def play()
  {
    begin()

    if (selectionPhase1(dealer.turnUpCandidateCard()) || selectionPhase2())
    {
      playRandomly()
      finalizeScore()
      partie.gameDone(this)
    }
  }

}
