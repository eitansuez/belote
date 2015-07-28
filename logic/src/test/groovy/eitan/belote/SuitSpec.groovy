package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suit.Carreau
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Pique
import static eitan.belote.Suit.Trefle

class SuitSpec extends Specification
{
  def "suit #suit should have eight cards"(suit) {
    expect:
    suit.cards.size() == 8

    where:
    suit << Suit.values()
  }

  def "can interpret suit from its name, irrespective of capitalization"()
  {
    expect:
    Suit.fromName("trEfle") == Trefle
    Suit.fromName("carreau") == Carreau
    Suit.fromName("Coeur") == Coeur
    Suit.fromName("PIQUE") == Pique
  }

}
