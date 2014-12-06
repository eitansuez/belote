package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.*
import static eitan.belote.Suite.Coeur
import static eitan.belote.Suite.Trefle

class GameSpec extends Specification
{
  Deck deck
  Player eitan, rony, johnny, corinne
  Game game

  def setup()
  {
    deck = Spy(Deck)

    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")

    game = new Game(deck: deck,
        team1: new Team(first: eitan, second: rony),
        team2: new Team(first: johnny, second: corinne))
  }

  def "should be able to construct a game with two teams and a card deck"()
  {
    when:
    game.begin()

    then:
    1 * deck.deal(_)
    eitan.cards.size() == 5
    deck.size() == 12
    game.scoreFor(game.team1) == 0
    game.scoreFor(game.team2) == 0
  }

  def "initially no atout is set"()
  {
    expect:
    game.atout == null
  }

  def "envoi should set atout and designate team"()
  {
    given:
    game.begin()

    when:
    game.envoi(Trefle, eitan)

    then:
    game.atout == Trefle
    game.committedPlayer == eitan
    game.committedTeam == game.team1
  }

  def "after envoi should know points for cards"()
  {
    given:
    game.begin()

    when:
    game.envoi(Trefle, eitan)

    then:
    game.points(new Card(type: Ace, suite: Trefle)) == 11
    game.points(new Card(type: Neuf, suite: Trefle)) == 14
    game.points(new Card(type: Valet, suite: Trefle)) == 20

    game.points(new Card(type: Ace, suite: Coeur)) == 11
    game.points(new Card(type: Neuf, suite: Coeur)) == 0
    game.points(new Card(type: Valet, suite: Coeur)) == 2
  }

  def "envoi should trigger deal remaining cards"()
  {
    given:
    game.begin()

    when:
    game.envoi(Trefle, eitan)

    then:
    1 * deck.dealRemaining(_)
    deck.empty()
    game.starter == eitan
  }

  def "play first hand"()
  {
    given:
    game.begin()
    game.envoi(Trefle, eitan)

    when:
    def cards = [
        new Card(type: Ace, suite: Coeur),
        new Card(type: Dix, suite: Coeur),
        new Card(type: Sept, suite: Trefle),
        new Card(type: Dame, suite: Coeur)]

    game.playHand(cards)

    then:
    game.scoreFor(game.team1) == 24
    game.scoreFor(game.team2) == 0
    game.starter == rony
  }

  def "cards drawn from players"()
  {
    given:
    game.begin()
    game.envoi(Trefle, eitan)

    when:
    game.playRandomHand()

    then:
    [eitan, rony, corinne, johnny].each { player ->
      player.cards.size() == 4
    }
  }

}
