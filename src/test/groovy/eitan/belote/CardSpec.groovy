package eitan.belote

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Neuf
import static eitan.belote.Suite.Carreau
import static eitan.belote.Suite.Coeur

class CardSpec extends Specification
{
  @Shared Card aceDeCarreau  = new Card(type: Ace, suite: Carreau)
  @Shared Card neufDeCoeur = new Card(type: Neuf, suite: Coeur)

  @Unroll
  def "should have card title reflecting type and suite"(card, title, points)
  {
    expect:
    card.title() == title
    card.points() == points

    where:
    card         | title            | points
    aceDeCarreau | "Ace de Carreau" | 11
    neufDeCoeur  | "Neuf de Coeur"  | 0
  }

}
