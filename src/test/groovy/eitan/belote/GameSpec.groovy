package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.*
import static Suit.Coeur
import static Suit.Trefle

class GameSpec extends Specification
{
  Deck deck
  Player eitan, rony, johnny, corinne
  List<Player> players
  Game game
  Partie partie

  def setup()
  {
    deck = GroovySpy(Deck)

    eitan = GroovySpy(Player, constructorArgs: [[name: 'Eitan']]) {
      envoi(_) >> true
    }

    johnny = GroovySpy(Player, constructorArgs: [[name: 'Johnny']])
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")

    players = [eitan, johnny, rony, corinne]

    partie = new Partie(
        team1: new Team(first: eitan, second: rony),
        team2: new Team(first: johnny, second: corinne)
    )

    partie.begin()

    game = partie.nextGame()
    game.deck = deck // so can spy
  }

  def "should be able to construct a game with two teams and a card deck"()
  {
    when:
    game.begin()

    then:
    1 * deck.deal(_)
    eitan.hand.size() == 5
    deck.size() == 12
    game.scores[game.team1] == 0
    game.scores[game.team2] == 0
  }

  def "initially no atout is set"()
  {
    expect:
    game.atout == null
  }

  def "envoi should set atout and designate team"()
  {
    when:
    game.begin()

    then:
    5 * eitan.dealCard(_)
    5 * johnny.dealCard(_)

    when:
    Card card = deck.takeCard()
    game.envoi(card, eitan)

    then:
    game.atout == card.suit
    game.committedPlayer == eitan
    game.committedTeam == game.team1
  }

  def "after envoi should know points for cards"()
  {
    given:
    game.begin()

    when:
    game.envoi(deck.takeCard(), eitan)

    then:
    game.points(new Card(type: Ace, suit: game.atout)) == 11
    game.points(new Card(type: Neuf, suit: game.atout)) == 14
    game.points(new Card(type: Valet, suit: game.atout)) == 20

    Suit other = Suit.values().find { Suit suit -> suit != game.atout }

    game.points(new Card(type: Ace, suit: other)) == 11
    game.points(new Card(type: Neuf, suit: other)) == 0
    game.points(new Card(type: Valet, suit: other)) == 2
  }

  def "envoi should trigger deal remaining cards"()
  {
    given:
    game.begin()

    when:
    Card card = deck.takeCard()
    game.envoi(card, eitan)

    then:
    1 * deck.dealRemaining(_, _, _)
    deck.empty()
    game.starter == eitan
  }

  def "play first round"()
  {
    given:
    game.begin()
    Card card = deck.takeCard()
    game.envoi(card, eitan)
    game.atout = Trefle

    when:
    def cards = [
        new Card(type: Ace, suit: Coeur),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Trefle),
        new Card(type: Dame, suit: Coeur)
    ]

    game.playRound(new Round(cards: cards, players: players, atout: game.atout))

    then:
    game.scores[game.team1] == 24
    game.scores[game.team2] == 0
    game.starter == rony
  }

  def "cards drawn from players"()
  {
    given:
    game.begin()
    Card card = deck.takeCard()
    game.envoi(card, eitan)

    when:
    game.playRandomRound()

    then:
    players.each { player ->
      player.hand.size() == 4
    }
    !game.done
  }

  def "players should have no more cards after eight rounds"()
  {
    given:
    game.begin()
    Card card = deck.takeCard()
    game.envoi(card, eitan)

    when:
    game.playRandomly()
    game.finalizeScore()

    then:
    players.each { player ->
      player.hand.empty
    }
    game.rounds.size() == 8
    int totalScore = game.scores[game.team1] + game.scores[game.team2]
    totalScore == 162 || totalScore == 250
    game.done
  }

  def "score finalization adds dix dedere"() {
    given:
    game.begin()
    game.envoi(deck.takeCard(), eitan)
    game.atout = Trefle  // override atout

    game.playRandomly()
    game.scores[game.team1] = 142
    game.scores[game.team2] = 10
    game.rounds.last().winner = eitan

    when:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 152
    game.winningTeam == game.team1
    game.losingTeam == game.team2
  }

  def "score finalization handles capot"() {
    given:
    game.begin()
    game.envoi(deck.takeCard(), eitan)
    game.atout = Trefle  // override atout

    game.playRandomly()
    game.scores[game.team1] = 152
    game.scores[game.team2] = 0

    when:
    game.finalizeScore()

    then:
    game.capot()
    game.scores[game.team1] == 252
    game.scores[game.team2] == 0
    game.winningTeam == game.team1
    game.losingTeam == game.team2
  }

  def "winning team is capot should also amount to 252 points for winner"() {
    given:
    game.begin()
    game.envoi(deck.takeCard(), eitan)
    game.atout = Trefle  // override atout
    game.playRandomly()
    game.scores[game.team1] = 0
    game.scores[game.team2] = 152

    when:
    game.finalizeScore()

    then:
    game.dedans()
    game.capot()
    game.scores[game.team1] == 0
    game.scores[game.team2] == 252
    game.winningTeam == game.team2
    game.losingTeam == game.team1
  }

  def "score finalization handles dedans"() {
    given:
    game.begin()
    game.envoi(deck.takeCard(), eitan)
    game.atout = Trefle  // override atout
    game.playRandomly()
    game.scores[game.team1] = 80
    game.scores[game.team2] = 72
    game.rounds.last().winner = corinne

    when:
    game.finalizeScore()

    then:
    game.dedans()
    game.scores[game.team1] == 0
    game.scores[game.team2] == 162
    game.winningTeam == game.team2
    game.losingTeam == game.team1
  }

  def "score finalization handles litige"() {
    given:
    game.begin()
    game.envoi(deck.takeCard(), eitan)
    game.atout = Trefle  // override atout
    game.playRandomly()
    game.scores[game.team1] = 81
    game.scores[game.team2] = 71
    game.rounds.last().winner = corinne

    when:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 81
    game.scores[game.team2] == 81
    game.litige()
    game.winningTeam == null
    game.losingTeam == null
  }

  def "players ordering from eitan"()
  {
    when:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = eitan

    then:
    game.players() == [eitan, johnny, rony, corinne]
  }

  def "players ordering from johnny"()
  {
    when:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = johnny

    then:
    game.players() == [johnny, rony, corinne, eitan]
  }

  def "players ordering from rony"()
  {
    when:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = rony

    then:
    game.players() == [rony, corinne, eitan, johnny]
  }

  def "players ordering from corinne"()
  {
    when:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = corinne

    then:
    game.players() == [corinne, eitan, johnny, rony]
  }

  def "should stop at third player"()
  {
    when:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = eitan

    then:
    game.players() == [eitan, johnny, rony, corinne]

    when:
    int count = 0
    game.withEachPlayerUntilReturns { player ->
      count += 1
      (player == rony) // interpret as:  stop at player == rony
    }

    then:
    count == 3
  }

  def "should iterate through players at most 4 times"()
  {
    when:
    int count = 0
    game.withEachPlayerUntilReturns { player ->
      count += 1
      false
    }

    then:
    count == 4
  }


  def "players dealt 5 cards after game start"()
  {
    when:
    game.begin()

    then:
    5 * eitan.dealCard(_)
    5 * johnny.dealCard(_)
  }


  def "after selection phase, players dealt remaining cards, and committed player set"()
  {
    when:
    game.begin()
    game.selectionPhase1(deck.takeCard())

    then:
    game.committedPlayer == eitan
    game.committedTeam == game.team1
    8 * eitan.dealCard(_)
    8 * johnny.dealCard(_)
    1 * deck.dealRemaining(players, eitan, _)
  }

  def "everyone passes, game is done, and score is 0"()
  {
    when:
    game.begin()
    Player overrideStub = new Player(name: "Eitan") // override stub, everyone now passes
    game.team1.first = overrideStub
    game.starter = overrideStub
    game.selectionPhase1(deck.takeCard())

    then:
    game.done
    game.scores[game.team1] == 0
    game.scores[game.team2] == 0
    game.atout == null
  }

}
