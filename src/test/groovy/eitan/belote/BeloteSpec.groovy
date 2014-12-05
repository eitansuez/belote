package eitan.belote

import spock.lang.Specification

class BeloteSpec extends Specification
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

    game = new Game(deck: deck, team1: [eitan, rony], team2: [johnny, corinne])
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
    when:
    eitan.dealCard(deck.takeCard())

    then:
    deck.size() == 31
    eitan.cards.size() == 1
  }

  def "should deal a whole deck"()
  {
    when:
    deck.deal(eitan, rony, johnny, corinne)

    then:
    deck.size() == 12
  }

  def "should be able to deal remainder after selection phase"()
  {
    given:
    deck.deal(eitan, rony, johnny, corinne)

    when:
    deck.dealRemaining(eitan, rony, johnny, corinne)

    then:
    deck.cards.empty
  }

  def "should be able to construct a game with two teams and a card deck"()
  {
    when:
    game.start()

    then:
    1 * deck.deal(_)
    eitan.cards.size() == 5
  }

}
