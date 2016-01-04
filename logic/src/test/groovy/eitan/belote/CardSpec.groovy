package eitan.belote

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Dame
import static eitan.belote.CardType.Dix
import static eitan.belote.CardType.Huit
import static eitan.belote.CardType.Neuf
import static eitan.belote.Suit.Carreau
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Trefle
import static eitan.belote.CardType.Sept
import static eitan.belote.CardType.Valet
import static eitan.belote.Suit.Pique

class CardSpec extends Specification
{
  @Shared Card aceDeCarreau = new Card(type: Ace, suit: Carreau)
  @Shared Card neufDeCoeur = new Card(type: Neuf, suit: Coeur)

  @Unroll
  def "expect card titles to match pattern <card type> de <suit>"(card)
  {
    expect:
    card.toString() =~ /\w+ de \w+/

    where:
    card << [aceDeCarreau, neufDeCoeur]
  }

  @Unroll
  def "should have title reflecting type and suit, and points"(Card card, String title, int points)
  {
    expect:
    card.toString() == title
    card.points(Suit.values().find { it != card.suit }) == points

    where:
    card         | title            | points
    aceDeCarreau | "Ace de Carreau" | 11
    neufDeCoeur  | "Neuf de Coeur"  | 0
  }

  @Unroll
  def "#card1 and #card2 are not equal"(card1, card2)
  {
    expect:
    card1 != card2

    where:
    card1                            | card2
    new Card(type: Dix, suit: Coeur) | new Card(type: Neuf, suit: Coeur)
    aceDeCarreau                     | neufDeCoeur
    new Card(type: Dix, suit: Coeur) | new Card(type: Dix, suit: Trefle)
  }

  def "equality.."()
  {
    expect:
    new Card(type: Dix, suit: Trefle) == new Card(type: Dix, suit: Trefle)
  }

  def "should unmarshal card from name"()
  {
    when:
    def cardName = "Dix_de_Coeur"

    then:
    Card.fromName(cardName) == new Card(type: Dix, suit: Coeur)
  }

  def "unmarshal from name is flexible, allows spaces instead of underscores"()
  {
    when:
    def cardName = "Dix de Coeur"

    then:
    Card.fromName(cardName) == new Card(type: Dix, suit: Coeur)
  }

  def "an eight of Trefle should beat a seven of Trefle when atout is #atout"(Suit atout) {
    when:
    def seven = new Card(type: Sept, suit: Trefle)
    def eight = new Card(type: Huit, suit: Trefle)

    then:
    eight.higherThan(seven, atout)

    where:
    atout << [Trefle, Pique, Coeur, Carreau]
  }

  def "a valet should beat a dame when atout"() {
    expect:
    new Card(type: Valet, suit: Trefle).higherThan(new Card(type: Dame, suit: Trefle), Trefle)
    new Card(type: Dame, suit: Trefle).higherThan(new Card(type: Valet, suit: Trefle), Coeur)
  }
}
