package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.*
import static eitan.belote.Suit.Carreau
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Pique
import static eitan.belote.Suit.Trefle

class PlayerSpec extends Specification
{
  Deck deck
  Player eitan, rony, johnny, corinne
  Team team1, team2

  def setup()
  {
    deck = new Deck()

    eitan = new Player(name: "Eitan")
    rony = new Player(name: "Rony")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")

    team1 = new Team(first: eitan, second: rony)
    team2 = new Team(first: johnny, second: corinne)
  }

  def "player can be dealt a card"()
  {
    when:
    eitan.receiveCard(deck.takeCard())

    then:
    deck.size() == 31
    eitan.hand.size() == 1
  }

  def "player can be dealt multiple cards"()
  {
    when:
    eitan.receiveCards(deck.takeCards(3))

    then:
    deck.size() == 29
    eitan.hand.size() == 3
  }

  def "player can play a card"()
  {
    given:
    eitan.receiveCards(deck.takeCards(8))
    def firstCard = eitan.hand.first()

    when:
    def played = eitan.playCard(firstCard, Trefle)

    then:
    eitan.hand.size() == 7
    played == firstCard
  }


  def "toString is meaningful"()
  {
    expect:
    eitan.toString() == "Eitan"
  }

  def "attempt to play a card not in player's hand should throw an exception"()
  {
    given:
    eitan.receiveCard(new Card(suit: Pique, type: Sept))

    when:
    eitan.playCard(new Card(suit: Pique, type: Huit), Trefle)

    then:
    thrown(AssertionError)
  }

  def "player 1 can play any card"()
  {
    given:
    eitan.receiveCards(deck.takeCards(8))

    when:
    def round = new Round(cards: [], players: [], game: new Game(atout: Trefle))
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }

  def "player 2 must follow suit"()
  {
    given:
    eitan.receiveCard(new Card(type: Ace, suit: Trefle))
    eitan.receiveCard(new Card(type: Dix, suit: Coeur))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Pique))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Coeur)]

    when:
    def round = new Round(cards: placed, players: [corinne], game: new Game(atout: Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Dix, suit: Coeur), new Card(type: Dame, suit: Coeur)] as HashSet<Card>
  }


  def "player 2 must cut"()
  {
    given:
    eitan.receiveCard(new Card(type: Ace, suit: Trefle))
    eitan.receiveCard(new Card(type: Dix, suit: Coeur))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Trefle))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique)]

    when:
    def round = new Round(cards: placed, players: [corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Ace, suit: Trefle), new Card(type: Dame, suit: Trefle)] as HashSet<Card>
  }

  def "player 2 who cannot follow suit and cannot cut should play any card"()
  {
    given:
    eitan.receiveCard(new Card(type: Ace, suit: Coeur))
    eitan.receiveCard(new Card(type: Dix, suit: Coeur))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Carreau))

    def placed = [new Card(type: Huit, suit: Pique)]

    when:
    def round = new Round(cards: placed, players: [corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }

  def "player 3 must raise cut of previous player"()
  {
    given:
    eitan.receiveCard(new Card(type: Ace, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Trefle))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique), new Card(type: Dame, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Ace, suit: Trefle), new Card(type: Roi, suit: Trefle)] as HashSet<Card>
  }

  def "player 3 cannot raise cut, but must play an atout nonetheless"()
  {
    given:
    eitan.receiveCard(new Card(type: Ace, suit: Coeur))
    eitan.receiveCard(new Card(type: Roi, suit: Coeur))
    eitan.receiveCard(new Card(type: Sept, suit: Trefle))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique), new Card(type: Dame, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Sept, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 must raise"()
  {
    given:
    eitan.receiveCard(new Card(type: Ace, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Trefle))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Trefle), new Card(type: Dame, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Roi, suit: Trefle), new Card(type: Ace, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, an eight is above a seven"()
  {
    given:
    eitan.receiveCard(new Card(type: Huit, suit: Trefle))
    eitan.receiveCard(new Card(type: Dix, suit: Trefle))
    eitan.receiveCard(new Card(type: Ace, suit: Trefle))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Sept, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Huit, suit: Trefle),
            new Card(type: Dix, suit: Trefle),
            new Card(type: Ace, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 cannot raise"()
  {
    given:
    eitan.receiveCard(new Card(type: Dame, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Trefle))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Trefle), new Card(type: Ace, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], game: new Game(atout:Trefle))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Roi, suit: Trefle), new Card(type: Sept, suit: Trefle),
            new Card(type: Dame, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 doesn't have any"()
  {
    given:
    eitan.receiveCard(new Card(type: Dame, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Trefle))
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique), new Card(type: Ace, suit: Pique)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], game: new Game(atout:Pique))
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }

  def "player 4 doesn't have to cut because his partner is master, by virtue of having cut"()
  {
    given:
    eitan.receiveCard(new Card(type: Dame, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Pique))
    eitan.receiveCard(new Card(type: Huit, suit: Coeur))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [
        new Card(type: Dame, suit: Carreau),
        new Card(type: Huit, suit: Pique),
        new Card(type: Ace, suit: Carreau)
    ]

    when:
    def round = new Round(cards: placed, players: [johnny, rony, corinne], game: new Game(atout:Pique))
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }


  def "player 4 doesn't have to cut because his partner is master, period."()
  {
    given:
    eitan.receiveCard(new Card(type: Dame, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Pique))
    eitan.receiveCard(new Card(type: Huit, suit: Coeur))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [
        new Card(type: Dame, suit: Carreau),
        new Card(type: Ace, suit: Carreau),
        new Card(type: Roi, suit: Carreau)
    ]

    when:
    def round = new Round(cards: placed, players: [johnny, rony, corinne], game: new Game(atout:Pique))
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }


  def "player 4 has to cut because other team is master"()
  {
    given:
    eitan.receiveCard(new Card(type: Dame, suit: Trefle))
    eitan.receiveCard(new Card(type: Roi, suit: Trefle))
    eitan.receiveCard(new Card(type: Sept, suit: Pique))
    eitan.receiveCard(new Card(type: Huit, suit: Coeur))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))

    def placed = [
        new Card(type: Dame, suit: Carreau),
        new Card(type: Ace, suit: Carreau),
        new Card(type: Roi, suit: Pique)
    ]

    when:
    def round = new Round(cards: placed, players: [johnny, rony, corinne], game: new Game(atout:Pique))
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Sept, suit: Pique)] as HashSet<Card>
  }

  def "test partnership"()
  {
    expect:
    eitan.isMyPartner(rony)
    rony.isMyPartner(eitan)
    corinne.isMyPartner(johnny)
    johnny.isMyPartner(corinne)
    ! eitan.isMyPartner(corinne)
    ! eitan.isMyPartner(johnny)
    ! rony.isMyPartner(corinne)
    ! rony.isMyPartner(johnny)
  }

  def "can plug in a different strategy"()
  {
    when:
    Player joe = new Player(name: "Joe", strategy: new TestStrategy())

    then:
    joe.strategy.class == TestStrategy
    joe.strategy.player == joe
  }

  def "player has belote rebelote"()
  {
    when:
    eitan.receiveCards([
        new Card(type: Dame, suit: Trefle),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Pique),
        new Card(type: Huit, suit: Pique),
        new Card(type: Roi, suit: Trefle)
    ])

    then:
    eitan.hasBeloteRebelote(Trefle)
    !eitan.hasBeloteRebelote(Coeur)
  }

  def "belote rebelote claim"()
  {
    when:
    eitan.setBeloteRebelote(true)
    def cards = [
        new Card(type: Dame, suit: Trefle),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Pique),
        new Card(type: Huit, suit: Pique),
        new Card(type: Roi, suit: Trefle)
    ]
    eitan.receiveCards(cards)

    then:
    eitan.beloteRebeloteClaim(cards[0], Trefle) == 'Belote'
  }

  def "belote rebelote claim next card"()
  {
    when:
    eitan.setBeloteRebelote(true)
    def cards = [
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Pique),
        new Card(type: Huit, suit: Pique),
        new Card(type: Roi, suit: Trefle)
    ]
    eitan.receiveCards(cards)

    then:
    eitan.beloteRebeloteClaim(cards[0], Trefle) == ''
  }

  def "belote rebelote claim second card"()
  {
    when:
    eitan.setBeloteRebelote(true)
    def cards = [
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Pique),
        new Card(type: Huit, suit: Pique),
        new Card(type: Roi, suit: Trefle)
    ]
    eitan.receiveCards(cards)

    then:
    eitan.beloteRebeloteClaim(cards[3], Trefle) == 'Rebelote'
  }

  def "belote rebelote claim post cards"()
  {
    when:
    eitan.setBeloteRebelote(true)
    def cards = [
        new Card(type: Dix, suit: Coeur),
        new Card(type: Sept, suit: Pique),
        new Card(type: Huit, suit: Pique)
    ]
    eitan.receiveCards(cards)

    then:
    eitan.beloteRebeloteClaim(cards[2], Trefle) == ''
  }

  def "hand should be sorted by suit, points, ordinal"()
  {
    when:
    eitan.receiveCard(new Card(type: Huit, suit: Carreau))
    eitan.receiveCard(new Card(type: Dame, suit: Coeur))
    eitan.receiveCard(new Card(type: Dix, suit: Coeur))
    eitan.receiveCard(new Card(type: Sept, suit: Carreau))
    eitan.receiveCard(new Card(type: Ace, suit: Coeur))
    eitan.receiveCard(new Card(type: Neuf, suit: Pique))

    then:
    eitan.hand == [
        new Card(type: Dame, suit: Coeur),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Ace, suit: Coeur),
        new Card(type: Neuf, suit: Pique),
        new Card(type: Sept, suit: Carreau),
        new Card(type: Huit, suit: Carreau)
      ]
  }

  def "hand should be sorted by suit, points, ordinal, atouts special"()
  {
    when:
    eitan.receiveCard(new Card(type: Huit, suit: Carreau), Trefle)
    eitan.receiveCard(new Card(type: Dame, suit: Coeur), Trefle)
    eitan.receiveCard(new Card(type: Ace, suit: Trefle), Trefle)
    eitan.receiveCard(new Card(type: Dix, suit: Coeur), Trefle)
    eitan.receiveCard(new Card(type: Sept, suit: Carreau), Trefle)
    eitan.receiveCard(new Card(type: Ace, suit: Coeur), Trefle)
    eitan.receiveCard(new Card(type: Neuf, suit: Trefle), Trefle)

    then:
    eitan.hand == [
        new Card(type: Dame, suit: Coeur),
        new Card(type: Dix, suit: Coeur),
        new Card(type: Ace, suit: Coeur),
        new Card(type: Sept, suit: Carreau),
        new Card(type: Huit, suit: Carreau),
        new Card(type: Ace, suit: Trefle),
        new Card(type: Neuf, suit: Trefle)
    ]
  }
}
