package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.Huit
import static eitan.belote.CardType.Sept
import static eitan.belote.Suite.Pique

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
    eitan.hand.size() == 1
  }

  def "player can be dealt multiple cards"()
  {
    when:
    eitan.dealCards(deck.takeCards(3))

    then:
    deck.size() == 29
    eitan.hand.size() == 3
  }

  def "player can play a card"()
  {
    given:
    eitan.dealCards(deck.takeCards(8))
    def firstCard = eitan.hand.first()

    when:
    def played = eitan.playCard(firstCard)

    then:
    eitan.hand.size() == 7
    played == firstCard
  }


  def "toString is meaningful"()
  {
    expect:
    eitan.toString() == "Player: Eitan"
  }

  def "attempt to play a card not in player's hand should throw an exception"()
  {
    given:
    eitan.dealCard(new Card(suite: Pique, type: Sept))

    when:
    eitan.playCard(new Card(suite: Pique, type: Huit))

    then:
    thrown(NoSuchElementException)
  }

}
