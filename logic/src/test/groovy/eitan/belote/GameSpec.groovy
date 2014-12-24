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

    // override game with a spy constructed the same way
    def gameSpy = GroovySpy(Game, constructorArgs: [[partie: partie, actorRef: partie.actorRef]])
    game = partie.nextGame(gameSpy)

    deck = GroovySpy(Deck)
    game.dealer = GroovySpy(Dealer)
    game.dealer.deck = deck // so can spy
  }

  def "should be able to construct a game with two teams and a card deck"()
  {
    when:
    game.begin()

    then:
    1 * game.dealer.deal(_)
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
    5 * eitan.receiveCard(_)
    5 * johnny.receiveCard(_)

    when:
    Card card = game.dealer.turnUpCandidateCard()
    game.envoi(card.suit, eitan)

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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)

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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)

    then:
    1 * game.dealer.dealRemaining(_, _)
    deck.empty()
    game.starter == eitan
  }

  def "play first round"()
  {
    given:
    game.begin()
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)
    game.atout = Trefle

    when:
    def cards = [
        new Card(type: Ace, suit: Coeur),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Trefle),
        new Card(type: Dame, suit: Coeur)
    ]

    def round = new Round(cards: cards, players: players, atout: game.atout)
    round.resolve()
    game.roundDone(round)

    then:
    game.scores[game.team1] == 24
    game.scoreAdjustment(game.team1) == -4
    game.scores[game.team2] == 0
    game.scoreAdjustment(game.team2) == 0
    game.starter == rony
  }

  def "cards drawn from players"()
  {
    given:
    game.begin()
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)

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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)

    when:
    game.playRandomly()
    game.addBeloteRebelote() >> { }  // mock method to ensure get no interference from possible dealing of belote rebelote
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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)
    game.atout = Trefle  // override atout

    game.playRandomly()
    game.scores[game.team1] = 142
    game.scores[game.team2] = 10
    game.rounds.last().winner = eitan

    game.addBeloteRebelote() >> { }  // mock method to ensure get no interference from possible dealing of belote rebelote

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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)
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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)
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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)
    game.atout = Trefle  // override atout
    game.playRandomly()
    game.scores[game.team1] = 80
    game.scores[game.team2] = 72
    game.rounds.last().winner = corinne

    game.addBeloteRebelote() >> { }  // mock method to ensure get no interference from possible dealing of belote rebelote

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
    game.envoi(game.dealer.turnUpCandidateCard().suit, eitan)
    game.atout = Trefle  // override atout
    game.playRandomly()
    game.scores[game.team1] = 81
    game.scores[game.team2] = 71
    game.rounds.last().winner = corinne

    game.addBeloteRebelote() >> { }  // mock method to ensure get no interference from possible dealing of belote rebelote

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
    5 * eitan.receiveCard(_)
    5 * johnny.receiveCard(_)
  }


  def "after selection phase, players dealt remaining cards, and committed player set"()
  {
    when:
    game.begin()
    game.selectionPhase1(game.dealer.turnUpCandidateCard())

    then:
    game.committedPlayer == eitan
    game.committedTeam == game.team1
    8 * eitan.receiveCard(_)
    8 * johnny.receiveCard(_)
    1 * game.dealer.dealRemaining(players, eitan)
  }

  def "everyone passes first round, game is not done, and score is 0"()
  {
    when:
    game.begin()
    setupPlayerToPass()
    def commit = game.selectionPhase1(game.dealer.turnUpCandidateCard())

    then:
    !commit
    !game.done
    game.atout == null
  }

  def "everyone passes, game is done, and score is 0"()
  {
    when:
    game.begin()
    setupPlayerToPass()
    game.selectionPhase1(game.dealer.turnUpCandidateCard())
    game.selectionPhase2()

    then:
    game.scores[game.team1] == 0
    game.scores[game.team2] == 0
    game.done
    game.atout == null
  }

  private void setupPlayerToPass()
  {
    Player player = new Player(name: "Eitan")
    game.team1.first = player
    game.starter = player
  }

  private void setupPlayerToEnvoiA(Suit suit) {
    Player player = GroovySpy(Player, constructorArgs: [[name: 'Eitan']]) {
      envoi() >> suit
    }
    game.team1.first = player
    game.starter = player
  }

  def "envoi occurs during second round of selection phase"()
  {
    given:
    game.begin()

    setupPlayerToPass()
    Card turnedUp = game.dealer.turnUpCandidateCard()
    game.selectionPhase1(turnedUp)

    Suit other = Suit.values().find { Suit suit -> suit != game.atout }
    setupPlayerToEnvoiA(other)

    when:
    game.selectionPhase2()

    then:
    game.committedPlayer == game.team1.first
    game.atout == other
    ! game.done
    1 * game.dealer.dealRemaining(_, _)
  }


  def "detect player with belote rebelote"()
  {
    given:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = partie.starter

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
    game.dealer.turnUpCandidateCard()
    game.envoi(Trefle, eitan)

    then:
    game.beloteRebelote() == eitan
  }

  def "this game has no belote rebelote"()
  {
    given:
    game.team1 = partie.team1
    game.team2 = partie.team2
    game.starter = partie.starter

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
    game.dealer.turnUpCandidateCard()
    game.envoi(Trefle, eitan)

    then:
    game.beloteRebelote() == null
  }

  def "score accounts for belote rebelote for team of player bearing the cards"()
  {
    given:
    game.team1 = partie.team1
    game.team2 = partie.team2
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
    game.envoi(Trefle, eitan)

    game.playRandomly()
    game.scores[game.team1] = 100
    game.scores[game.team2] = 52
    game.rounds.last().winner = eitan

    when:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 130
    game.scores[game.team2] == 52
  }

  def "score accounts for belote rebelote when other team bears it"()
  {
    given:
    game.team1 = partie.team1
    game.team2 = partie.team2
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
    game.envoi(Trefle, eitan)

    game.playRandomly()
    game.scores[game.team1] = 100
    game.scores[game.team2] = 52
    game.rounds.last().winner = eitan

    when:
    game.finalizeScore()

    then:
    game.scores[game.team1] == 110
    game.scores[game.team2] == 72
  }
}
