package eitan.belote

import spock.lang.Specification

class DeckSpec extends Specification
{
  Deck deck

  def setup()
  {
    deck = new Deck()
  }

  def "deck should have 32 cards"()
  {
    expect:
    deck.cards.size() == 32
  }

  def "deck is initially full"()
  {
    expect:
    deck.full()
  }

  def "should be able to take a card from deck"()
  {
    when:
    deck.takeCard()

    then:
    deck.cards.size() == 31
  }

  def "should be able to take n cards"()
  {
    when:
    def cards = deck.takeCards(3)

    then:
    deck.size() == 29
    cards.size() == 3
  }

}
