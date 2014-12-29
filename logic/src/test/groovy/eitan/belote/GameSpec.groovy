package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.*
import static Suit.Coeur
import static Suit.Trefle
import static eitan.belote.Suit.Pique

class GameSpec extends Specification
{
  Deck deck
  Player eitan, rony, johnny, corinne
  List<Player> players
  Game game
  Partie partie

  def setup()
  {
    eitan = GroovySpy(Player, constructorArgs: [[name: 'Eitan']])

    johnny = GroovySpy(Player, constructorArgs: [[name: 'Johnny']])
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")

    players = [eitan, johnny, rony, corinne]

    partie = GroovySpy(Partie, constructorArgs: [[team1: new Team(first: eitan, second: rony),
                                                  team2: new Team(first: johnny, second: corinne)]]) {
      gameDone(_) >> {}  // prevent next game from kicking off
    }

    partie.init()

    // override game with a spy constructed the same way
    def gameSpy = GroovySpy(Game, constructorArgs: [[partie: partie, actorRef: partie.actorRef]])

    game = partie.nextGame(gameSpy)

    deck = GroovySpy(Deck)
    game.dealer = GroovySpy(Dealer)
    game.dealer.deck = deck // so can spy
  }

  def "should be able to construct a game with two teams and a card deck"()
  {
    given:
    game.startSelectionPhase1() >> {}

    when:
    game.begin()

    then:
    1 * game.dealer.deal(_)
    eitan.hand.size() == 5
    deck.size() == 11
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
    given:
    game.startSelectionPhase1() >> {}
    game.startPlayPhase() >> {}

    when:
    game.begin()

    then:
    5 * eitan.receiveCard(_)
    5 * johnny.receiveCard(_)

    when:
    def candidate = game.dealer.candidate
    game.nextPlayer()
    game.envoi()

    then:
    game.atout == candidate.suit
    game.committedPlayer == eitan
    game.committedTeam == game.team1
  }

  def "after envoi should know points for cards"()
  {
    given:
    game.startSelectionPhase1() >> {}
    game.startPlayPhase() >> {}

    when:
    game.begin()

    and:
    game.nextPlayer()
    game.envoi()

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
    game.startSelectionPhase1() >> {}
    game.startPlayPhase() >> {}

    when:
    game.begin()

    and:
    game.nextPlayer()
    game.envoi()

    then:
    1 * game.dealer.dealRemaining(_, _)
    deck.empty()
    game.starter == eitan
  }

  def "play first round"()
  {
    given:
    game.startSelectionPhase1() >> {}
    game.startPlayPhase() >> {}

    when:
    game.begin()


    and:
    game.nextPlayer()
    game.envoi()
    game.atout = Trefle

    and:
    def cards = [
        new Card(type: Ace, suit: Coeur),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Trefle),
        new Card(type: Dame, suit: Coeur)
    ]

    def round = new Round(cards: cards, players: players, game: game)
    round.resolve()

    then:
    game.scores[game.team1] == 24
    game.scoreAdjustment(game.team1) == -4
    game.scores[game.team2] == 0
    game.scoreAdjustment(game.team2) == 0
    game.nextStarter() == rony
  }

  def "cards drawn from players"()
  {
    given:
    eitan.offer(_, _) >> { Game game, Card card -> game.envoi() }
    game.continuePlayPhase() >> {}

    when:
    game.begin()

    then:
    players.each { player ->
      player.hand.size() == 4
    }
    !game.done
  }

  def "players should have no more cards after eight rounds"()
  {
    given:
    eitan.offer(_, _) >> { Game game, Card card -> game.envoi() }
    game.addBeloteRebelote() >> {}  // mock method to ensure get no interference from possible dealing of belote rebelote

    when:
    game.begin()

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
    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: eitan)
    game.rounds << lastRound
    game.scores[game.team1] = 142
    game.scores[game.team2] = 10
    game.committedPlayer = eitan

    when:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 152
    game.winningTeam == game.team1
    game.losingTeam == game.team2
  }

  def "score finalization handles capot"() {
    given:
    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: eitan)
    game.rounds << lastRound
    game.scores[game.team1] = 152
    game.scores[game.team2] = 0
    game.committedPlayer = eitan

    when:
    game.finalizeScore()

    then:
    !game.dedans()
    game.capot()
    game.scores[game.team1] == 252
    game.scores[game.team2] == 0
    game.winningTeam == game.team1
    game.losingTeam == game.team2
  }

  def "winning team is capot should also amount to 252 points for winner"() {
    given:
    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: eitan)
    game.rounds << lastRound
    game.scores[game.team1] = 0
    game.scores[game.team2] = 152
    game.committedPlayer = eitan

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
    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: corinne)
    game.rounds << lastRound
    game.scores[game.team1] = 80
    game.scores[game.team2] = 72
    game.committedPlayer = eitan

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
    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: corinne)
    game.rounds << lastRound
    game.scores[game.team1] = 81
    game.scores[game.team2] = 71
    game.committedPlayer = eitan

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
    game.starter = eitan

    then:
    game.players() == [eitan, johnny, rony, corinne]
  }

  def "players ordering from johnny"()
  {
    when:
    game.starter = johnny

    then:
    game.players() == [johnny, rony, corinne, eitan]
  }

  def "players ordering from rony"()
  {
    when:
    game.starter = rony

    then:
    game.players() == [rony, corinne, eitan, johnny]
  }

  def "players ordering from corinne"()
  {
    when:
    game.starter = corinne

    then:
    game.players() == [corinne, eitan, johnny, rony]
  }

  def "should stop at third player"()
  {
    when:
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
    given:
    game.startSelectionPhase1() >> {}

    when:
    game.begin()
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
    given:
    game.startSelectionPhase1() >> {}

    when:
    game.begin()

    then:
    5 * eitan.receiveCard(_)
    5 * johnny.receiveCard(_)
  }


  def "after selection phase, players dealt remaining cards, and committed player set"()
  {
    given:
    eitan.offer(_, _) >> { Game game, Card card -> game.envoi() }
    game.startSelectionPhase2() >> {}

    when:
    game.begin()

    then:
    game.committedPlayer == eitan
    game.committedTeam == game.team1
    8 * eitan.receiveCard(_)
    8 * johnny.receiveCard(_)
    1 * game.dealer.dealRemaining(players, eitan)
  }

  def "everyone passes first round, game is not done, and score is 0"()
  {
    given:
    eitan.offer(_, _) >> { Game game, Card candidate -> game.pass() }
    game.startSelectionPhase2() >> {}

    when:
    game.begin()

    then:
    !game.done
    game.atout == null
    game.committedPlayer == null
  }

  def "everyone passes, game is done, and score is 0"()
  {
    given:
    eitan.offer(_, _) >> { Game game, Card card -> game.pass() }
    eitan.offer(_) >> { Game game -> game.passDeuxFois() }

    when:
    game.begin()

    then:
    game.scores[game.team1] == 0
    game.scores[game.team2] == 0
    game.done
    game.forfeited()
    game.atout == null
  }

  def "envoi occurs during second round of selection phase"()
  {
    given:
    eitan.offer(_, _) >> { Game game, Card card -> game.pass() }
    eitan.offer(_) >> { Game game -> game.envoi(Pique) }
    game.startPlayPhase() >> {}

    when:
    game.begin()

    then:
    game.committedPlayer == game.team1.first
    game.atout == Pique
    ! game.done
    1 * game.dealer.dealRemaining(_, _)
  }


  def "detect player with belote rebelote"()
  {
    given:
    game.starter = eitan
    eitan.receiveCards([
        deck.takeSpecificCard(new Card(type: Dame, suit: Trefle)),
        deck.takeSpecificCard(new Card(type: Dix, suit: Coeur)),
        deck.takeSpecificCard(new Card(type: Sept, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Huit, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Roi, suit: Trefle))
    ])
    [rony, corinne, johnny].each { player ->
      game.dealer.dealToPlayer(player, deck.takeCards(5))
    }

    when:
    game.atout = Trefle

    then:
    game.beloteRebelote() == eitan
  }

  def "this game has no belote rebelote"()
  {
    given:
    game.starter == eitan

    eitan.receiveCards([
        deck.takeSpecificCard(new Card(type: Dame, suit: Trefle)),
        deck.takeSpecificCard(new Card(type: Dix, suit: Coeur)),
        deck.takeSpecificCard(new Card(type: Sept, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Huit, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Ace, suit: Trefle))
    ])
    [rony, corinne, johnny].each { player ->
      game.dealer.dealToPlayer(player, deck.takeCards(5))
    }

    when:
    game.atout = Trefle

    then:
    game.beloteRebelote() == null
  }

  def "score accounts for belote rebelote for team of player bearing the cards"()
  {
    given:
    game.startPlayPhase() >> {}
    eitan.offer(_,_) >> { Game game, Card candidate -> game.envoi(Trefle)}

    game.starter = partie.starter
    game.initScores()

    eitan.receiveCards([
        deck.takeSpecificCard(new Card(type: Dame, suit: Trefle)),
        deck.takeSpecificCard(new Card(type: Dix, suit: Coeur)),
        deck.takeSpecificCard(new Card(type: Sept, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Huit, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Roi, suit: Trefle))
    ])
    [rony, corinne, johnny].each { player ->
      game.dealer.dealToPlayer(player, deck.takeCards(5))
    }
    game.dealer.turnUpCandidateCard()

    when:
    game.startSelectionPhase1()

    and:
    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: eitan)
    game.rounds << lastRound
    game.scores[game.team1] = 100
    game.scores[game.team2] = 52

    and:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 130
    game.scores[game.team2] == 52
  }

  def "score accounts for belote rebelote when other team bears it"()
  {
    given:
    game.startPlayPhase() >> {}
    eitan.offer(_,_) >> { Game game, Card candidate -> game.envoi(Trefle)}

    game.starter = partie.starter
    game.initScores()

    corinne.receiveCards([
        deck.takeSpecificCard(new Card(type: Dame, suit: Trefle)),
        deck.takeSpecificCard(new Card(type: Dix, suit: Coeur)),
        deck.takeSpecificCard(new Card(type: Sept, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Huit, suit: Pique)),
        deck.takeSpecificCard(new Card(type: Roi, suit: Trefle))
    ])
    [eitan, rony, johnny].each { player ->
      game.dealer.dealToPlayer(player, deck.takeCards(5))
    }

    game.dealer.turnUpCandidateCard()

    game.startSelectionPhase1()

    game.isLastRound() >> true
    game.done = true
    def lastRound = new Round(winner: eitan)
    game.rounds << lastRound

    game.scores[game.team1] = 100
    game.scores[game.team2] = 52

    when:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 110
    game.scores[game.team2] == 72
  }
}
