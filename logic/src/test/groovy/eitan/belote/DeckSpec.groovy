package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.Dame
import static eitan.belote.Suit.Trefle

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

  def "take a specific card from the deck"()
  {
    when:
    def card = new Card(type: Dame, suit: Trefle)
    deck.takeSpecificCard(card)

    then:
    deck.size() == 31

    and:
    !deck.hasCard(card)
  }

}
