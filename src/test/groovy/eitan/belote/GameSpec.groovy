package eitan.belote

import spock.lang.Specification

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
    game.start()

    then:
    1 * deck.deal(_)
    eitan.cards.size() == 5
    deck.size() == 12
    game.team1.score == 0
    game.team2.score == 0
  }

}
