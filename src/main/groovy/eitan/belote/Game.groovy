package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Game
{
  Partie partie
  Deck deck = new Deck()

  Team team1, team2
  def scores = [:]

  Suit atout
  Player committedPlayer

  Player starter

  List<Round> rounds = []

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

  def selectionPhase1(Card candidate)
  {
    log.info("turning over candidate card ${candidate}")
    log.info("envoi a ${candidate.suit}?")
    def response = withEachPlayerUntilReturns { Player player ->
      if (player.envoi(candidate)) {
        log.info("${player} says yes for ${candidate.suit}")
        envoi(candidate, player)
        return [player, candidate]
      } else {
        log.info("${player} passes")
        return null
      }
    }

    if (!response) {
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
    deck.deal(players())
    showPlayerCards()
  }

  private void dealRemainingCards(Card chosenCard)
  {
    log.info("Dealing remaining cards..")
    deck.dealRemaining(players(), committedPlayer, chosenCard)
    showPlayerCards()
  }

  private void showPlayerCards()
  {
    withEachPlayer { Player player ->
      player.showHand()
    }
  }

  def envoi(Card chosenCard, Player player)
  {
    this.committedPlayer = player
    this.atout = chosenCard.suit
    log.info("Game starting with ${player} envoie a ${chosenCard.suit}")
    dealRemainingCards(chosenCard)
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
    log.info("Round #${rounds.size()+1}")

    def round = new Round(atout: atout)

    withEachPlayer { Player player ->
      Card selected = player.chooseCard(round)
      def card = player.playCard(selected)
      log.info("${player} plays ${card}")

      round = round.newRound(card, player)
    }

    playRound(round)
  }

  void playRound(Round round)
  {
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
