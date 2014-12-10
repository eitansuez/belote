package eitan.belote

import spock.lang.Specification

class SuitSpec extends Specification
{
  def "toString should return suit name"()
  {
    expect:
    Suit.Trefle.toString() == "Trefle"
  }

}
