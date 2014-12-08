package eitan.belote

class Game
{
  Partie partie
  Deck deck = new Deck()
  Team team1, team2
  Suite atout
  def committedPlayer
  def scores = [:]
  def rounds = []
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
    deck.deal(players())
  }

  def envoi(Suite suite, Player p)
  {
    this.committedPlayer = p
    this.atout = suite
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

    if (scoreFor(team1) > scoreFor(team2)) {
      return team1
    } else if (scoreFor(team2) > scoreFor(team1)) {
      return team2
    }
    null // a tie implies no winning team
  }

  int points(Card card)
  {
    card.points(atout)
  }

  int scoreFor(Team team) {
    scores[team]
  }

  void playRound(List<Card> cards)
  {
    assert cards.size() == 4
    def cardMap = [
        (cards[0]): team1.first,
        (cards[1]): team2.first,
        (cards[2]): team1.second,
        (cards[3]): team2.second]

    def round = new Round(cards: cardMap, atout: atout)
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
      return
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
    scoreFor(team1) == 0 || scoreFor(team2) == 0
  }

  boolean dedans() {
    scoreFor(committedTeam) < 81
  }

  boolean litige() {
    scoreFor(committedTeam) == 81
  }

  boolean isLastRound() {
    rounds.size() == 8
  }

  void playRandomRound()
  {
    def cards = []

    Player player = starter
    4.times {
      Card validCard = player.validCard(cards, atout)
      cards << player.playCard(validCard)
      player = partie.nextPlayer(player)
    }

    playRound(cards)
  }

  void playRandomly()
  {
    8.times { playRandomRound() }
  }
}
