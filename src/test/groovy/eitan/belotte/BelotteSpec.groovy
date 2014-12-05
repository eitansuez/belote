package eitan.belotte

import spock.lang.Specification

class BelotteSpec extends Specification
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

  def "should be able to take a card"()
  {
    when:
    deck.takeCard()

    then:
    deck.cards.size() == 31
  }

  def "should see the card"()
  {
    given:
    def card = deck.takeCard()

    when:
    String title = card.title()
    println title

    then:
    title =~ /\w+ de \w+/
  }


}
