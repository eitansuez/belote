package eitan.belote

import spock.lang.Specification

class DeckSpec extends Specification
{
  Deck deck
  Player eitan, rony, johnny, corinne

  def setup()
  {
    deck = new Deck()

    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")
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

  def "should deal 20 cards in first phase"()
  {
    when:
    deck.deal([eitan, rony, johnny, corinne])

    then:
    deck.size() == 12
  }

  def "should deal remainder of cards after selection phase"()
  {
    given:
    deck.deal([eitan, rony, johnny, corinne])
    Card chosen = deck.takeCard()

    when:
    deck.dealRemaining([eitan, rony, johnny, corinne], eitan, chosen)

    then:
    deck.empty()
  }

}
