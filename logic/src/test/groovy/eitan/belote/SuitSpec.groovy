package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suit.Carreau
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Pique
import static eitan.belote.Suit.Trefle

class SuitSpec extends Specification
{
  def "toString should return suit name"()
  {
    expect:
    Trefle.toString() == "Trefle"
  }

  def "interprets suite from acronym"()
  {
    expect:
    Suit.interpretSuitFromAcronym("c") == Trefle
    Suit.interpretSuitFromAcronym("h") == Coeur
    Suit.interpretSuitFromAcronym("s") == Pique
    Suit.interpretSuitFromAcronym("d") == Carreau
  }

  def "throws assertion error when for invalid suit acronym"()
  {
    when:
    Suit.interpretSuitFromAcronym("e")

    then:
    thrown(AssertionError)
  }

}
