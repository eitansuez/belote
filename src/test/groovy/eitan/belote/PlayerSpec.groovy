package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Dame
import static eitan.belote.CardType.Dix
import static eitan.belote.CardType.Huit
import static eitan.belote.CardType.Roi
import static eitan.belote.CardType.Sept
import static Suit.Carreau
import static Suit.Coeur
import static Suit.Pique
import static Suit.Trefle

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
    eitan.toString() == "Eitan"
  }

  def "attempt to play a card not in player's hand should throw an exception"()
  {
    given:
    eitan.dealCard(new Card(suit: Pique, type: Sept))

    when:
    eitan.playCard(new Card(suit: Pique, type: Huit))

    then:
    thrown(AssertionError)
  }

  def "player 1 can play any card"()
  {
    given:
    eitan.dealCards(deck.takeCards(8))

    when:
    def round = new Round(cards: [], players: [], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }

  def "player 2 must follow suit"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suit: Trefle))
    eitan.dealCard(new Card(type: Dix, suit: Coeur))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Pique))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Coeur)]

    when:
    def round = new Round(cards: placed, players: [corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Dix, suit: Coeur), new Card(type: Dame, suit: Coeur)] as HashSet<Card>
  }


  def "player 2 must cut"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suit: Trefle))
    eitan.dealCard(new Card(type: Dix, suit: Coeur))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Trefle))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique)]

    when:
    def round = new Round(cards: placed, players: [corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Ace, suit: Trefle), new Card(type: Dame, suit: Trefle)] as HashSet<Card>
  }

  def "player 2 who cannot follow suit and cannot cut should play any card"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suit: Coeur))
    eitan.dealCard(new Card(type: Dix, suit: Coeur))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Carreau))

    def placed = [new Card(type: Huit, suit: Pique)]

    when:
    def round = new Round(cards: placed, players: [corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }

  def "player 3 must raise cut of previous player"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Trefle))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique), new Card(type: Dame, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Ace, suit: Trefle), new Card(type: Roi, suit: Trefle)] as HashSet<Card>
  }

  def "player 3 cannot raise cut, but must play an atout nonetheless"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suit: Coeur))
    eitan.dealCard(new Card(type: Roi, suit: Coeur))
    eitan.dealCard(new Card(type: Sept, suit: Trefle))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique), new Card(type: Dame, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Sept, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 must raise"()
  {
    given:
    eitan.dealCard(new Card(type: Ace, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Trefle))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Trefle), new Card(type: Dame, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Roi, suit: Trefle), new Card(type: Ace, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 cannot raise"()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Trefle))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Trefle), new Card(type: Ace, suit: Trefle)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], atout: Trefle)
    def set = eitan.validCards(round)

    then:
    set == [new Card(type: Roi, suit: Trefle), new Card(type: Sept, suit: Trefle),
            new Card(type: Dame, suit: Trefle)] as HashSet<Card>
  }

  def "a round of atout, player 3 doesn't have any"()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Trefle))
    eitan.dealCard(new Card(type: Huit, suit: Carreau))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [new Card(type: Huit, suit: Pique), new Card(type: Ace, suit: Pique)]

    when:
    def round = new Round(cards: placed, players: [rony, corinne], atout: Pique)
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }

  def "player 4 doesn't have to cut because his partner is master, by virtue of having cut"()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Pique))
    eitan.dealCard(new Card(type: Huit, suit: Coeur))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [
        new Card(type: Dame, suit: Carreau),
        new Card(type: Huit, suit: Pique),
        new Card(type: Ace, suit: Carreau)
    ]

    when:
    def round = new Round(cards: placed, players: [johnny, rony, corinne], atout: Pique)
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }


  def "player 4 doesn't have to cut because his partner is master, period."()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Pique))
    eitan.dealCard(new Card(type: Huit, suit: Coeur))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [
        new Card(type: Dame, suit: Carreau),
        new Card(type: Ace, suit: Carreau),
        new Card(type: Roi, suit: Carreau)
    ]

    when:
    def round = new Round(cards: placed, players: [johnny, rony, corinne], atout: Pique)
    def set = eitan.validCards(round)

    then:
    set == eitan.hand as Set
  }


  def "player 4 has to cut because other team is master"()
  {
    given:
    eitan.dealCard(new Card(type: Dame, suit: Trefle))
    eitan.dealCard(new Card(type: Roi, suit: Trefle))
    eitan.dealCard(new Card(type: Sept, suit: Pique))
    eitan.dealCard(new Card(type: Huit, suit: Coeur))
    eitan.dealCard(new Card(type: Dame, suit: Coeur))

    def placed = [
        new Card(type: Dame, suit: Carreau),
        new Card(type: Ace, suit: Carreau),
        new Card(type: Roi, suit: Pique)
    ]

    when:
    def round = new Round(cards: placed, players: [johnny, rony, corinne], atout: Pique)
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
}
