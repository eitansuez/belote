package eitan.belote

class Game
{
  Deck deck = new Deck()
  Team team1, team2
  Suite atout
  def committedPlayer
  def scores = [:]
  def hands = []
  Player starter
  boolean done

  def begin()
  {
    done = false
    initScores()
    starter = team1.first
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

  int points(Card card)
  {
    card.points(atout)
  }

  int scoreFor(Team team) {
    scores[team]
  }

  void playHand(List<Card> cards)
  {
    assert cards.size() == 4
    def cardMap = [
        (cards[0]): team1.first,
        (cards[1]): team2.first,
        (cards[2]): team1.second,
        (cards[3]): team2.second]

    def hand = new Hand(cards: cardMap, atout: atout)
    hand.resolve()

    hands << hand

    def winnerTeam = hand.winner.team
    scores[winnerTeam] += hand.points
    if (lastHand())
    {
      addDixDedere(winnerTeam)
      done = true
    }
    else
    {
      starter = hand.winner
    }
  }

  private void addDixDedere(Team team)
  {
    scores[team] += 10
  }

  boolean lastHand() {
    hands.size() == 8
  }

  void playRandomHand()
  {
    def cards = []
    players().each { player ->
      cards << player.playRandomCard()
    }
    playHand(cards)
  }
}
