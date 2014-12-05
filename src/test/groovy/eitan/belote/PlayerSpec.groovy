package eitan.belote

import spock.lang.Specification

class PlayerSpec extends Specification
{
  Deck deck
  Player eitan, rony, johnny, corinne
  Game game

  def setup()
  {
    deck = new Deck()

    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")
  }

  def "player can be dealt a card"()
  {
    when:
    eitan.dealCard(deck.takeCard())

    then:
    deck.size() == 31
    eitan.cards.size() == 1
  }

}
