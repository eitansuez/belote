package eitan.belote

import spock.lang.Specification

class PlayerSpec extends Specification
{
  Deck deck
  Player eitan

  def setup()
  {
    deck = new Deck()
    eitan = new Player(name: "Eitan")
  }

  def "player can be dealt a card"()
  {
    when:
    eitan.dealCard(deck.takeCard())

    then:
    deck.size() == 31
    eitan.cards.size() == 1
  }

  def "player can be dealt multiple cards"()
  {
    when:
    eitan.dealCards(deck.takeCards(3))

    then:
    deck.size() == 29
    eitan.hand().size() == 3
  }

}
