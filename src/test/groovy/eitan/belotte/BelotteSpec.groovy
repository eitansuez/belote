package eitan.belotte

import spock.lang.Specification

class BelotteSpec extends Specification
{

  Deck deck
  List<Player> players

  def setup()
  {
    deck = new Deck()
    players = [new Player(), new Player(), new Player(), new Player()]
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

  def "should be able to take n cards"()
  {
    when:
    def cards = deck.takeCards(3)

    then:
    deck.size() == 29
    cards.size() == 3
  }

  def "player can be dealt a card"()
  {
    given:
    def player = players[0]

    when:
    player.dealCard(deck.takeCard())

    then:
    deck.size() == 31
    player.cards.size() == 1
  }

  def "should deal a whole deck"()
  {
    when:
    deck.deal(players)

    then:
    deck.size() == 12
  }

  def "should be able to deal remainder after selection phase"()
  {
    given:
    deck.deal(players)

    when:
    deck.dealRemaining(players)

    then:
    deck.cards.empty
  }


}
