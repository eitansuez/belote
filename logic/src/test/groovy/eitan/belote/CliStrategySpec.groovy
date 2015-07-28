package eitan.belote

import spock.lang.Specification

import static eitan.belote.CliStrategy.interpretFromAcronym
import static eitan.belote.Suit.Carreau
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Pique
import static eitan.belote.Suit.Trefle

class CliStrategySpec extends Specification
{
  def "interprets suite from acronym"()
  {
    expect:
    interpretFromAcronym("c") == Trefle
    interpretFromAcronym("h") == Coeur
    interpretFromAcronym("s") == Pique
    interpretFromAcronym("d") == Carreau
  }

  def "throws assertion error when for invalid suit acronym"()
  {
    when:
    interpretFromAcronym("e")

    then:
    thrown(AssertionError)
  }


}
