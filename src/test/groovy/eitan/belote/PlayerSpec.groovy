package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Dame
import static eitan.belote.CardType.Dix
import static eitan.belote.CardType.Huit
import static eitan.belote.CardType.Roi
import static eitan.belote.CardType.Sept
import static eitan.belote.Suite.Carreau
import static eitan.belote.Suite.Coeur
import static eitan.belote.Suite.Pique
import static eitan.belote.Suite.Trefle

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

  def "player 1 can play any card"()
  {
    given:
    eitan.dealCards(deck.takeCards(8))

    when:
    def set = eitan.validCards([], Trefle)

    then:
    set == eitan.hand
  }

  def "player 2 must follow suite"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suite: Trefle))
    eitan.dealCard(new Card(type: Dix, suite: Coeur))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Pique))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Coeur)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == [new Card(type: Dix, suite: Coeur), new Card(type: Dame, suite: Coeur)] as HashSet<Card>
  }


  def "player 2 must cut"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suite: Trefle))
    eitan.dealCard(new Card(type: Dix, suite: Coeur))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Trefle))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Pique)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == [new Card(type: Ace, suite: Trefle), new Card(type: Dame, suite: Trefle)] as HashSet<Card>
  }

  def "player 2 who cannot follow suite and cannot cut should play any card"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suite: Coeur))
    eitan.dealCard(new Card(type: Dix, suite: Coeur))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Carreau))

    def placed = [new Card(type: Huit, suite: Pique)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == eitan.hand
  }

  def "player 3 must raise cut of previous player"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suite: Trefle))
    eitan.dealCard(new Card(type: Roi, suite: Trefle))
    eitan.dealCard(new Card(type: Sept, suite: Trefle))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Pique), new Card(type: Dame, suite: Trefle)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == [new Card(type: Ace, suite: Trefle), new Card(type: Roi, suite: Trefle)] as HashSet<Card>
  }

  def "player 3 cannot raise cut, but must play an atout nonetheless"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suite: Coeur))
    eitan.dealCard(new Card(type: Roi, suite: Coeur))
    eitan.dealCard(new Card(type: Sept, suite: Trefle))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Pique), new Card(type: Dame, suite: Trefle)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == [new Card(type: Sept, suite: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 must raise"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suite: Trefle))
    eitan.dealCard(new Card(type: Roi, suite: Trefle))
    eitan.dealCard(new Card(type: Sept, suite: Trefle))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Trefle), new Card(type: Dame, suite: Trefle)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == [new Card(type: Roi, suite: Trefle), new Card(type: Ace, suite: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 cannot raise"()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suite: Trefle))
    eitan.dealCard(new Card(type: Roi, suite: Trefle))
    eitan.dealCard(new Card(type: Sept, suite: Trefle))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Trefle), new Card(type: Ace, suite: Trefle)]

    when:
    def set = eitan.validCards(placed, Trefle)

    then:
    set == [new Card(type: Roi, suite: Trefle), new Card(type: Sept, suite: Trefle),
            new Card(type: Dame, suite: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 doesn't have any"()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suite: Trefle))
    eitan.dealCard(new Card(type: Roi, suite: Trefle))
    eitan.dealCard(new Card(type: Sept, suite: Trefle))
    eitan.dealCard(new Card(type: Huit, suite: Carreau))
    eitan.dealCard(new Card(type: Dame, suite: Coeur))

    def placed = [new Card(type: Huit, suite: Pique), new Card(type: Ace, suite: Pique)]

    when:
    def set = eitan.validCards(placed, Pique)

    then:
    set == eitan.hand
  }
}
