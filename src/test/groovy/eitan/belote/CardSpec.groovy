package eitan.belote

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Dix
import static eitan.belote.CardType.Neuf
import static eitan.belote.Suite.Carreau
import static eitan.belote.Suite.Coeur
import static eitan.belote.Suite.Trefle

class CardSpec extends Specification
{
  @Shared Card aceDeCarreau  = new Card(type: Ace, suite: Carreau)
  @Shared Card neufDeCoeur = new Card(type: Neuf, suite: Coeur)

  @Unroll
  def "should have card title reflecting type and suite"(card, title, points)
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
    card1                                | card2
    new Card(type: Dix, suite: Coeur)    | new Card(type: Neuf, suite: Coeur)
    aceDeCarreau | neufDeCoeur
    new Card(type: Dix, suite: Coeur) | new Card(type: Dix, suite: Trefle)
  }

  def "equality.."()
  {
    expect:
    new Card(type: Dix, suite: Trefle) == new Card(type: Dix, suite: Trefle)
  }

}
