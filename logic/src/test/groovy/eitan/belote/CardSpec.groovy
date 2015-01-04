package eitan.belote

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Dix
import static eitan.belote.CardType.Neuf
import static Suit.Carreau
import static Suit.Coeur
import static Suit.Trefle

class CardSpec extends Specification
{
  @Shared Card aceDeCarreau = new Card(type: Ace, suit: Carreau)
  @Shared Card neufDeCoeur = new Card(type: Neuf, suit: Coeur)


  @Unroll
  def "expect card titles to match pattern <cardtype> de <suit>"(card)
  {
    expect:
    card.toString() =~ /\w+ de \w+/

    where:
    card << [aceDeCarreau, neufDeCoeur]
  }

  @Unroll
  def "should have card title reflecting type and suit, and points"(card, title, points)
  {
    expect:
    card.toString() == title
    card.points() == points

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

  def "unmarshal from name"()
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

}
